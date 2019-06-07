package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.util.JsonUtilities.*;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.filebot.Cache;
import net.filebot.CacheType;
import net.filebot.Resource;

public enum Manami implements ArtworkProvider {

	AniDB;

	public String getURI(int id) {
		return "https://anidb.net/a" + id;
	}

	public String getIdentifier() {
		return name().toLowerCase();
	}

	@Override
	public List<Artwork> getArtwork(int id, String category, Locale locale) throws Exception {
		List<Artwork> artwork = new ArrayList<Artwork>(1);

		Optional<URI> picture = getRecord(id).map(r -> getStringValue(r, "picture", URI::create)).filter(r -> r.getPath().endsWith(".jpg"));
		if (picture.isPresent()) {
			artwork.add(new Artwork(Stream.of("picture"), picture.get().toURL(), null, null));
		}

		return artwork;
	}

	public Optional<Map<?, ?>> getRecord(int id) throws Exception {
		String uri = getURI(id);

		return getRecords().filter(r -> {
			return stream(getArray(r, "sources")).anyMatch(uri::equals);
		}).findFirst();
	}

	public Stream<Map<?, ?>> getRecords() throws Exception {
		return streamJsonObjects(database.get(), "data");
	}

	public Set<Integer> getDeadEntries() throws Exception {
		return stream(getArray(deadEntries.get(), getIdentifier())).map(Object::toString).map(Integer::parseInt).collect(toSet());
	}

	protected static Cache getCache() {
		return Cache.getCache("manami", CacheType.Persistent);
	}

	protected static Object request(String file) throws Exception {
		// NOTE: GitHub only supports If-None-Match (If-Modified-Since is ignored)
		Cache cache = getCache();

		return cache.json(file, Manami::getResource).fetch(fetchIfNoneMatch(url -> file, cache)).expire(Cache.ONE_MONTH).get();
	}

	protected static URL getResource(String file) throws Exception {
		return new URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/" + file);
	}

	protected static final Resource<Object> database = Resource.lazy(() -> request("anime-offline-database.json"));
	protected static final Resource<Object> deadEntries = Resource.lazy(() -> request("dead-entries.json"));

}
