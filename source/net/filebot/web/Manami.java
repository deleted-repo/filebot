package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.util.JsonUtilities.*;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.Icon;

import net.filebot.Cache;
import net.filebot.CacheType;
import net.filebot.Resource;

public class Manami implements Datasource {

	public static final Manami INSTANCE = new Manami();

	@Override
	public String getIdentifier() {
		return "Minami";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	protected Cache getCache() {
		return Cache.getCache(getIdentifier(), CacheType.Persistent);
	}

	protected Object request(String file) throws Exception {
		return getCache().json(file, this::getResource).fetch(fetchIfNoneMatch(URL::getPath, getCache())).expire(Cache.ONE_MONTH).get();
	}

	protected URL getResource(String file) throws Exception {
		return new URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/" + file);
	}

	protected Object getDatabase() throws Exception {
		return request("anime-offline-database.json");
	}

	protected Object getDeadEntries() throws Exception {
		return request("dead-entries.json");
	}

	protected final Resource<Object> database = Resource.lazy(this::getDatabase);
	protected final Resource<Object> deadEntries = Resource.lazy(this::getDeadEntries);

	public Stream<Map<?, ?>> getRecords() throws Exception {
		return streamJsonObjects(database.get(), "data");
	}

	public Optional<Map<?, ?>> getRecord(String uri) throws Exception {
		return getRecords().filter(r -> {
			return stream(getArray(r, "sources")).anyMatch(uri::equals);
		}).findFirst();
	}

	public Optional<URI> getPicture(String uri) throws Exception {
		return getRecord(uri).map(r -> getStringValue(r, "picture", URI::create)).filter(r -> r.getPath().endsWith(".jpg"));
	}

	public Set<Integer> getDeadEntries(Source source) throws Exception {
		return stream(getArray(deadEntries.get(), source.getIdentifier())).map(Object::toString).map(Integer::parseInt).collect(toSet());
	}

	public enum Source {

		AniDB;

		public String getURI(int id) {
			switch (this) {
			case AniDB:
				return "https://anidb.net/a" + id;
			}
			return null;
		}

		public String getIdentifier() {
			return name().toLowerCase();
		}
	}

}
