package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.util.JsonUtilities.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import net.filebot.util.ByteBufferOutputStream;
import net.filebot.util.SystemProperty;
import net.filebot.web.WebRequest;

public class License implements Serializable {

	private String product;
	private String id;
	private Instant expires;

	private Exception error;

	public License(File file) {
		try {
			// read and verify license file
			if (!file.exists()) {
				throw new FileNotFoundException("License not found");
			}

			byte[] bytes = Files.readAllBytes(file.toPath());

			// verify and get clear signed content
			Map<String, String> properties = getProperties(bytes);

			this.product = properties.get("Product");
			this.id = properties.get("Order");
			this.expires = LocalDate.parse(properties.get("Valid-Until"), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).plusDays(1).minusSeconds(1).toInstant();

			// verify license online
			verifyLicense(bytes);
		} catch (Exception e) {
			error = e;
		}
	}

	private Map<String, String> getProperties(byte[] bytes) throws Exception {
		return NEWLINE.splitAsStream(verifyClearSignMessage(bytes)).map(s -> s.split(": ", 2)).collect(toMap(a -> a[0], a -> a[1]));
	}

	private String verifyClearSignMessage(byte[] bytes) throws Exception {
		ArmoredInputStream armoredInput = new ArmoredInputStream(new ByteArrayInputStream(bytes));

		// read content
		ByteBufferOutputStream content = new ByteBufferOutputStream(256);
		int character;

		while ((character = armoredInput.read()) >= 0 && armoredInput.isClearText()) {
			content.write(character);
		}

		// read public key
		PGPPublicKeyRing publicKeyRing = new PGPPublicKeyRing(License.class.getResourceAsStream("license.key"), new JcaKeyFingerprintCalculator());
		PGPPublicKey publicKey = publicKeyRing.getPublicKey();

		// read signature
		PGPSignatureList signatureList = (PGPSignatureList) new JcaPGPObjectFactory(armoredInput).nextObject();
		PGPSignature signature = signatureList.get(0);
		signature.init(new JcaPGPContentVerifierBuilderProvider(), publicKey);

		// normalize clear sign message
		String clearSignMessage = NEWLINE.splitAsStream(UTF_8.decode(content.getByteBuffer())).map(String::trim).collect(joining("\r\n"));

		// verify signature
		signature.update(clearSignMessage.getBytes(UTF_8));

		if (!signature.verify()) {
			throw new PGPException("Bad Signature");
		}

		return clearSignMessage;
	}

	private void verifyLicense(byte[] bytes) throws Exception {
		Cache cache = CacheManager.getInstance().getCache("license", CacheType.Persistent);
		Object json = cache.json(id, i -> new URL("https://license.filebot.net/verify/" + i)).fetch((url, modified) -> WebRequest.post(url, bytes, "application/octet-stream", null)).expire(Cache.ONE_MONTH).get();

		if (getInteger(json, "status") != 200) {
			throw new PGPException(getString(json, "message"));
		}
	}

	public void check() throws Exception {
		if (error != null) {
			throw error;
		}

		if (Instant.now().isAfter(expires)) {
			throw new IllegalStateException("Expired: " + toString());
		}
	}

	@Override
	public String toString() {
		return String.format("%s License %s (Valid-Until: %s)", product, id, expires == null ? null : expires.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	public static final SystemProperty<File> FILE = SystemProperty.of("net.filebot.license", File::new, ApplicationFolder.AppData.resolve("license.txt"));
	public static final MemoizedResource<License> INSTANCE = Resource.lazy(() -> new License(FILE.get()));

	public static License configure(File file) throws Exception {
		// lock memoized resource while validating and setting a new license
		synchronized (INSTANCE) {
			// check if license file is valid and not expired
			License license = new License(file);
			license.check();

			// write to default license file path
			Files.copy(file.toPath(), FILE.get().toPath(), StandardCopyOption.REPLACE_EXISTING);

			// clear memoized instance and reload on next access
			INSTANCE.clear();

			return license;
		}
	}

}
