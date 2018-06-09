package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.ByteArrayInputStream;
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

	private byte[] bytes;

	private long id;
	private long expires;

	public License(byte[] bytes) throws Exception {
		this.bytes = bytes;

		// verify and get clear signed content
		Map<String, String> properties = getProperties();

		this.id = Long.parseLong(properties.get("Order"));
		this.expires = LocalDate.parse(properties.get("Valid-Until"), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).plusDays(1).minusSeconds(1).toInstant().toEpochMilli();

		// verify license online
		verifyLicense();
	}

	public boolean expired() {
		return expires > System.currentTimeMillis();
	}

	public Map<String, String> getProperties() throws Exception {
		return NEWLINE.splitAsStream(verifyClearSignMessage()).map(s -> s.split(": ", 2)).collect(toMap(a -> a[0], a -> a[1]));
	}

	public String verifyClearSignMessage() throws Exception {
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
			throw new PGPException("Signature does not match");
		}

		return clearSignMessage;
	}

	private void verifyLicense() throws Exception {
		Cache cache = CacheManager.getInstance().getCache("license", CacheType.Persistent);
		String message = new CachedResource<Long, String>(id, i -> new URL("https://license.filebot.net/verify/" + id), (url, modified) -> WebRequest.post(url, bytes, "application/octet-stream", null), getText(UTF_8), String.class::cast, Cache.ONE_MONTH, cache).get().trim();

		if (!message.equals("OK")) {
			throw new PGPException(message);
		}
	}

	@Override
	public String toString() {
		return String.format("%s (Valid-Until: %s)", id, Instant.ofEpochMilli(expires).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

}
