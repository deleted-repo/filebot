package net.filebot.web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

public enum ThumbnailProvider {

	TheTVDB, TheMovieDB;

	public URI getThumbnailURL(int id) {
		return URI.create("https://api.filebot.net/images/" + name().toLowerCase() + "/thumb/poster/" + id + ".png");
	}

	public byte[][] getThumbnails(int[] ids) throws Exception {
		HttpClient client = HttpClient.newHttpClient();

		CompletableFuture<HttpResponse<byte[]>>[] request = new CompletableFuture[ids.length];
		byte[][] response = new byte[ids.length][];

		for (int i = 0; i < request.length; i++) {
			HttpRequest r = HttpRequest.newBuilder(getThumbnailURL(ids[i])).build();
			request[i] = client.sendAsync(r, BodyHandlers.ofByteArray());
		}

		for (int i = 0; i < request.length; i++) {
			HttpResponse<byte[]> r = request[i].get();
			if (r.statusCode() == 200) {
				response[i] = r.body();
			}
		}

		return response;
	}

}
