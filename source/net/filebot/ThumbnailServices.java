package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.ResourceManager.*;
import static net.filebot.util.RegularExpressions.*;

import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.tukaani.xz.XZInputStream;

import net.filebot.web.SearchResult;
import net.filebot.web.ThumbnailProvider;

public enum ThumbnailServices implements ThumbnailProvider {

	TheTVDB, TheMovieDB;

	protected String getResource(String file) {
		return "https://api.filebot.net/images/" + name().toLowerCase() + "/thumb/poster/" + file;
	}

	protected String getThumbnailResource(int id, ResolutionVariant variant) {
		return variant == ResolutionVariant.NORMAL ? id + ".png" : id + "@2x.png";
	}

	protected Cache getCache(ResolutionVariant variant) {
		return Cache.getCache("thumbnail_" + ordinal() + "_" + variant.ordinal(), CacheType.Persistent);
	}

	protected Set<Integer> getIndex() throws Exception {
		Cache cache = getCache(ResolutionVariant.NORMAL);
		byte[] bytes = cache.bytes("index.txt.xz", n -> new URL(getResource(n)), XZInputStream::new).expire(Cache.ONE_MONTH).get();

		// all data files are UTF-8 encoded XZ compressed text files
		return NEWLINE.splitAsStream(UTF_8.decode(ByteBuffer.wrap(bytes))).filter(s -> s.length() > 0).map(Integer::parseInt).collect(toSet());
	}

	private final Resource<Set<Integer>> index = Resource.lazy(this::getIndex);

	// shared HTTP Client instance for all thumbnail requests
	private static final Resource<HttpClient> http = Resource.lazy(HttpClient::newHttpClient);

	public byte[][] getThumbnails(int[] ids, ResolutionVariant variant) throws Exception {
		Cache cache = getCache(variant);
		byte[][] response = new byte[ids.length][];

		synchronized (index) {
			// check cache
			for (int i = 0; i < response.length; i++) {
				response[i] = (byte[]) cache.get(ids[i]);
			}

			// create if necessary
			CompletableFuture<HttpResponse<byte[]>>[] request = new CompletableFuture[ids.length];

			for (int i = 0; i < response.length; i++) {
				if (response[i] == null && index.get().contains(ids[i])) {
					String resource = getThumbnailResource(ids[i], variant);
					request[i] = http.get().sendAsync(HttpRequest.newBuilder(URI.create(resource)).build(), BodyHandlers.ofByteArray());

					debug.fine(format("Request %s", resource));
				}
			}

			for (int i = 0; i < response.length; i++) {
				if (request[i] != null) {
					try {
						HttpResponse<byte[]> r = request[i].get();

						response[i] = r.statusCode() == 200 ? r.body() : new byte[0];
						cache.put(ids[i], response[i]);

						debug.finest(format("Received %,d bytes (%d %s)", r.body().length, r.statusCode(), r.uri()));
					} catch (Exception e) {
						debug.warning(e::toString);
					}
				}
			}

			return response;
		}
	}

	@Override
	public Map<SearchResult, Icon> getThumbnails(List<SearchResult> keys) throws Exception {
		ResolutionVariant variant = PRIMARY_SCALE_FACTOR > 1 ? ResolutionVariant.RETINA : ResolutionVariant.NORMAL;

		int[] ids = keys.stream().mapToInt(SearchResult::getId).toArray();
		byte[][] thumbnails = getThumbnails(ids, variant);

		Map<SearchResult, Icon> icons = new HashMap<>(thumbnails.length);
		for (int i = 0; i < thumbnails.length; i++) {
			if (thumbnails[i] != null && thumbnails[i].length > 0) {
				try {
					icons.put(keys.get(i), getScaledIcon(thumbnails[i], variant));
				} catch (Exception e) {
					debug.log(Level.SEVERE, e, e::toString);
				}
			}
		}

		return icons;
	}

	protected Icon getScaledIcon(byte[] bytes, ResolutionVariant variant) throws Exception {
		// Load multi-resolution images only if necessary
		if (PRIMARY_SCALE_FACTOR == 1 && variant == ResolutionVariant.NORMAL) {
			return new ImageIcon(bytes);
		}

		BufferedImage baseImage = ImageIO.read(new ByteArrayInputStream(bytes));
		double baseScale = variant.scaleFactor;

		List<BufferedImage> image = new ArrayList<BufferedImage>(3);
		image.add(baseImage);

		// use down-scaled @2x image as @1x base image
		if (baseScale > 1) {
			image.add(0, scale(1 / baseScale, baseImage));
		}

		// Windows 10: use down-scaled @2x image for non-integer scale factors 1.25 / 1.5 / 1.75
		if (PRIMARY_SCALE_FACTOR > 1 && PRIMARY_SCALE_FACTOR < 2 && image.size() >= 2) {
			image.add(1, scale(PRIMARY_SCALE_FACTOR / 2, image.get(1)));
		}

		return new ImageIcon(new BaseMultiResolutionImage(image.toArray(Image[]::new)));
	}

	public enum ResolutionVariant {

		NORMAL(1), RETINA(2);

		public final int scaleFactor;

		private ResolutionVariant(int scaleFactor) {
			this.scaleFactor = scaleFactor;
		}
	}

}
