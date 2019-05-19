package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.ResourceManager.*;
import static net.filebot.util.RegularExpressions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
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

	TheTVDB, TheMovieDB, AniDB;

	protected String getResource(String file) {
		return "https://api.filebot.net/images/" + name().toLowerCase() + "/thumb/poster/" + file;
	}

	protected String getThumbnailResource(int id, ResolutionVariant variant) {
		switch (variant) {
		case NORMAL:
			return getResource(id + ".png");
		default:
			return getResource(id + "@2x.png");
		}
	}

	protected Cache getCache(ResolutionVariant variant) {
		return Cache.getCache("thumbnail_" + ordinal() + "_" + variant.ordinal(), CacheType.Persistent);
	}

	protected Set<Integer> getIndex() throws Exception {
		Cache cache = getCache(ResolutionVariant.NORMAL);
		byte[] bytes = cache.bytes(0, n -> new URL(getResource("index.txt.xz")), XZInputStream::new).expire(Cache.ONE_MONTH).get();

		// all data files are UTF-8 encoded XZ compressed text files
		return NEWLINE.splitAsStream(UTF_8.decode(ByteBuffer.wrap(bytes))).map(Integer::parseInt).collect(toSet());
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
	public Map<SearchResult, Icon> getThumbnails(List<SearchResult> keys, ResolutionVariant variant) throws Exception {
		int[] ids = keys.stream().mapToInt(SearchResult::getId).toArray();
		byte[][] thumbnails = getThumbnails(ids, variant);

		Map<SearchResult, Icon> icons = new HashMap<>(thumbnails.length);
		for (int i = 0; i < thumbnails.length; i++) {
			if (thumbnails[i] != null && thumbnails[i].length > 0) {
				try {
					icons.put(keys.get(i), getIcon(thumbnails[i], variant));
				} catch (Exception e) {
					debug.log(Level.SEVERE, e, e::toString);
				}
			}
		}

		return icons;
	}

	protected Icon getIcon(byte[] bytes, ResolutionVariant variant) throws Exception {
		BufferedImage baseImage = ImageIO.read(new ByteArrayInputStream(bytes));
		double baseScale = variant.scaleFactor;

		return new ImageIcon(getMultiResolutionImage(baseImage, baseScale));
	}

}
