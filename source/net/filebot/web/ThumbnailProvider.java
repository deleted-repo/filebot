package net.filebot.web;

import static net.filebot.Logging.*;
import static net.filebot.util.FileUtilities.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

import net.filebot.Cache;
import net.filebot.CacheType;

public enum ThumbnailProvider {

	TheTVDB, TheMovieDB;

	public String getThumbnailResource(int id) {
		return "https://api.filebot.net/images/" + name().toLowerCase() + "/thumb/poster/" + id + ".png";
	}

	public synchronized byte[][] getThumbnails(int[] ids) throws Exception {
		HttpClient http = HttpClient.newHttpClient();

		CompletableFuture<HttpResponse<byte[]>>[] request = new CompletableFuture[ids.length];
		byte[][] response = new byte[ids.length][];

		// check cache
		for (int i = 0; i < response.length; i++) {
			response[i] = (byte[]) cache.get(ids[i]);
		}

		for (int i = 0; i < response.length; i++) {
			if (response[i] == null) {
				HttpRequest r = HttpRequest.newBuilder(URI.create(getThumbnailResource(ids[i]))).build();
				request[i] = http.sendAsync(r, BodyHandlers.ofByteArray());

				debug.fine(format("Fetch resource: %s", r.uri()));
			}
		}

		for (int i = 0; i < response.length; i++) {
			if (request[i] != null) {
				HttpResponse<byte[]> r = request[i].get();
				if (r.statusCode() == 200) {
					response[i] = r.body();
				} else {
					response[i] = new byte[0];
				}

				cache.put(ids[i], response[i]);

				debug.finest(format("Received %s (%s)", formatSize(response[i].length), r.uri()));
			}
		}

		return response;
	}

	// per instance cache
	private final Cache cache = Cache.getCache("thumbnail_" + ordinal(), CacheType.Monthly);

}
