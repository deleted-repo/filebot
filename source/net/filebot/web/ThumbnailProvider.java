package net.filebot.web;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.util.FileUtilities.*;
import static net.filebot.util.RegularExpressions.*;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.tukaani.xz.XZInputStream;

import net.filebot.Cache;
import net.filebot.CacheType;
import net.filebot.Resource;

public enum ThumbnailProvider {

	TheTVDB, TheMovieDB;

	protected String getResourceLocation(String file) {
		return "https://api.filebot.net/images/" + name().toLowerCase() + "/thumb/poster/" + file;
	}

	protected Set<Integer> getIndex() throws Exception {
		byte[] bytes = cache.bytes("index.txt.xz", n -> new URL(getResourceLocation(n)), XZInputStream::new).expire(Cache.ONE_MONTH).get();

		// all data files are UTF-8 encoded XZ compressed text files
		return NEWLINE.splitAsStream(UTF_8.decode(ByteBuffer.wrap(bytes))).filter(s -> s.length() > 0).map(Integer::parseInt).collect(toSet());
	}

	private final Resource<Set<Integer>> index = Resource.lazy(this::getIndex);

	public byte[][] getThumbnails(int[] ids) throws Exception {
		synchronized (index) {
			CompletableFuture<HttpResponse<byte[]>>[] request = new CompletableFuture[ids.length];
			byte[][] response = new byte[ids.length][];

			// check cache
			for (int i = 0; i < response.length; i++) {
				response[i] = (byte[]) cache.get(ids[i]);
			}

			// create if necessary
			Resource<HttpClient> http = Resource.lazy(HttpClient::newHttpClient);

			for (int i = 0; i < response.length; i++) {
				if (response[i] == null && index.get().contains(ids[i])) {
					HttpRequest r = HttpRequest.newBuilder(URI.create(getResourceLocation(ids[i] + ".png"))).build();
					request[i] = http.get().sendAsync(r, BodyHandlers.ofByteArray());

					debug.fine(format("Request %s", r.uri()));
				}
			}

			for (int i = 0; i < response.length; i++) {
				if (request[i] != null) {
					HttpResponse<byte[]> r = request[i].get();

					response[i] = r.statusCode() == 200 ? r.body() : new byte[0];
					cache.put(ids[i], response[i]);

					debug.finest(format("Received %s (%s)", formatSize(response[i].length), r.uri()));
				}
			}

			return response;
		}
	}

	// per instance cache
	private final Cache cache = Cache.getCache("thumbnail_" + ordinal(), CacheType.Monthly);

}
