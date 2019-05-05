package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.util.RegularExpressions.*;

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

	protected Cache getCache() {
		return Cache.getCache("thumbnail_" + ordinal(), CacheType.Persistent);
	}

	protected Set<Integer> getIndex() throws Exception {
		byte[] bytes = getCache().bytes("index.txt.xz", n -> new URL(getResource(n)), XZInputStream::new).expire(Cache.ONE_MONTH).get();

		// all data files are UTF-8 encoded XZ compressed text files
		return NEWLINE.splitAsStream(UTF_8.decode(ByteBuffer.wrap(bytes))).filter(s -> s.length() > 0).map(Integer::parseInt).collect(toSet());
	}

	private final Resource<Set<Integer>> index = Resource.lazy(this::getIndex);

	public byte[][] getThumbnails(int[] ids) throws Exception {
		Cache cache = getCache();
		byte[][] response = new byte[ids.length][];

		synchronized (index) {
			// check cache
			for (int i = 0; i < response.length; i++) {
				response[i] = (byte[]) cache.get(ids[i]);
			}

			// create if necessary
			CompletableFuture<HttpResponse<byte[]>>[] request = new CompletableFuture[ids.length];
			Resource<HttpClient> http = Resource.lazy(HttpClient::newHttpClient);

			for (int i = 0; i < response.length; i++) {
				if (response[i] == null && index.get().contains(ids[i])) {
					URI r = URI.create(getResource(ids[i] + ".png"));
					request[i] = http.get().sendAsync(HttpRequest.newBuilder(r).build(), BodyHandlers.ofByteArray());

					debug.fine(format("Request %s", r));
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
		int[] ids = keys.stream().mapToInt(SearchResult::getId).toArray();
		byte[][] thumbnails = getThumbnails(ids);

		Map<SearchResult, Icon> icons = new HashMap<>(thumbnails.length);
		for (int i = 0; i < thumbnails.length; i++) {
			if (thumbnails[i] != null && thumbnails[i].length > 0) {
				try {
					icons.put(keys.get(i), new ImageIcon(thumbnails[i]));
				} catch (Exception e) {
					debug.log(Level.SEVERE, e, e::toString);
				}
			}
		}

		return icons;
	}

}