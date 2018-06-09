package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.util.FileUtilities.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
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
import net.filebot.web.WebRequest;

public class License implements Serializable {

	public static final File LICENSE_FILE = ApplicationFolder.AppData.resolve("license.txt");
	public static final Resource<License> INSTANCE = Resource.lazy(() -> new License(readFile(LICENSE_FILE)));

	private int id;
	private long expires;

	public License(byte[] bytes) throws Exception {
		String content = verifyClearSignMessage(new ByteArrayInputStream(bytes), License.class.getResourceAsStream("license.key"));

		// parse properties file
		Map<String, String> properties = NEWLINE.splitAsStream(content).map(s -> s.split(": ", 2)).collect(toMap(a -> a[0], a -> a[1]));

		this.id = Integer.parseInt(properties.get("Order"));
		this.expires = LocalDate.parse(properties.get("Valid-Until"), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).plusDays(1).minusSeconds(1).toInstant().toEpochMilli();

		System.out.println(properties);

		// verify license online first
		verifyLicense(id, bytes);
	}

	@Override
	public String toString() {
		return String.format("%s (Valid-Until: %s)", id, Instant.ofEpochMilli(expires).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	public boolean isValid() {
		return expires < System.currentTimeMillis();
	}

	private void verifyLicense(int id, byte[] file) throws Exception {
		Cache cache = CacheManager.getInstance().getCache("license", CacheType.Persistent);
		String message = new CachedResource<Integer, String>(id, i -> new URL("https://license.filebot.net/verify.cgi?order=" + id), (url, modified) -> WebRequest.post(url, file, "application/octet-stream", null), getText(UTF_8), String.class::cast, Cache.ONE_MONTH, cache).get().trim();

		if (!message.equals("OK")) {
			throw new PGPException(message);
		}
	}

	private String verifyClearSignMessage(InputStream clearSignFile, InputStream publicKeyFile) throws Exception {
		ArmoredInputStream armoredInput = new ArmoredInputStream(clearSignFile);

		// read content
		ByteBufferOutputStream content = new ByteBufferOutputStream(256);
		int character;

		while ((character = armoredInput.read()) >= 0 && armoredInput.isClearText()) {
			content.write(character);
		}

		// read public key
		PGPPublicKeyRing publicKeyRing = new PGPPublicKeyRing(publicKeyFile, new JcaKeyFingerprintCalculator());
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
			throw new PGPException("BAD LICENSE: Signature does not match");
		}

		return clearSignMessage;
	}

}
