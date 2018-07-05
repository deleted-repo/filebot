package net.filebot;

import static java.util.stream.Collectors.*;
import static net.filebot.Settings.*;
import static net.filebot.util.JsonUtilities.*;
import static net.filebot.util.PGP.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

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
				throw new FileNotFoundException("UNREGISTERED");
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

	private String getExpirationDate() {
		return expires == null ? null : expires.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private Map<String, String> getProperties(byte[] bytes) throws Exception {
		byte[] pub = IOUtils.toByteArray(getClass().getResource("license.key"));
		String msg = verifyClearSignMessage(bytes, pub);

		return NEWLINE.splitAsStream(msg).map(s -> s.split(": ", 2)).collect(toMap(a -> a[0], a -> a[1]));
	}

	private void verifyLicense(byte[] bytes) throws Exception {
		Cache cache = CacheManager.getInstance().getCache("license", CacheType.Persistent);
		Object json = cache.json(id, i -> new URL("https://license.filebot.net/verify/" + i)).fetch((url, modified) -> WebRequest.post(url, bytes, "application/octet-stream", getRequestParameters())).expire(Cache.ONE_MONTH).get();

		if (getInteger(json, "status") != 200) {
			throw new IllegalStateException(getString(json, "message"));
		}
	}

	private Map<String, String> getRequestParameters() {
		Map<String, String> parameters = new HashMap<String, String>(2);

		// add standard HTTP headers
		parameters.put("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));

		// add custom HTTP headers for user statistics
		parameters.put("X-FileBot-OS", getSystemIdentifier());
		parameters.put("X-FileBot-PKG", getApplicationDeployment().toUpperCase());

		return parameters;
	}

	public License check() throws Exception {
		if (error != null) {
			throw error;
		}

		if (Instant.now().isAfter(expires)) {
			throw new IllegalStateException("EXPIRED since " + getExpirationDate());
		}

		return this;
	}

	@Override
	public String toString() {
		return String.format("%s License %s (Valid-Until: %s)", product, id, getExpirationDate());
	}

	public static final SystemProperty<File> FILE = SystemProperty.of("net.filebot.license", File::new, ApplicationFolder.AppData.resolve("license.txt"));
	public static final MemoizedResource<License> INSTANCE = Resource.lazy(() -> new License(FILE.get()));

	public static License importLicenseFile(File file) throws Exception {
		// lock memoized resource while validating and setting a new license
		synchronized (License.INSTANCE) {
			// check if license file is valid and not expired
			License license = new License(file).check();

			// write to default license file path
			Files.copy(file.toPath(), License.FILE.get().toPath(), StandardCopyOption.REPLACE_EXISTING);

			// clear memoized instance and reload on next access
			License.INSTANCE.clear();

			// return valid license object
			return license;
		}
	}

}
