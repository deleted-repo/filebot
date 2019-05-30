package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.util.JsonUtilities.*;
import static net.filebot.web.WebRequest.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.filebot.Cache;
import net.filebot.CacheType;

public class XEM {

	public List<Map<String, Map<String, Integer>>> getAll(String origin, int id) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(2);
		parameters.put("origin", origin);
		parameters.put("id", id);

		Object response = request("all", parameters);
		return (List) asList(getArray(response, "data"));
	}

	public Map<String, Map<String, Integer>> getSingle(String origin, int id, int season, int episode) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("origin", origin);
		parameters.put("id", id);
		parameters.put("season", season);
		parameters.put("episode", episode);

		Object response = request("single", parameters);
		return (Map) getMap(response, "data");
	}

	public List<SearchResult> getAllNames(String origin) throws Exception {
		return getAllNames(origin, null, null, true);
	}

	public List<SearchResult> getAllNames(String origin, Integer season, String language, boolean defaultNames) throws Exception {
		List<SearchResult> result = new ArrayList<>();

		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("origin", origin);
		parameters.put("season", season);
		parameters.put("language", language);
		parameters.put("defaultNames", defaultNames ? "1" : "0");

		Object response = request("allNames", parameters);

		getMap(response, "data").forEach((k, v) -> {
			int id = Integer.parseInt(k.toString());
			List<String> names = stream(asArray(v)).filter(Objects::nonNull).map(Objects::toString).filter(s -> s.length() > 0).collect(toList());

			if (names.size() > 0) {
				result.add(new SearchResult(id, names.get(0), names.subList(1, names.size())));
			}
		});

		return result;
	}

	public Set<Integer> getHaveMap(String origin) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(1);
		parameters.put("origin", origin);

		Object response = request("havemap", parameters);
		return stream(getArray(response, "data")).map(Object::toString).map(Integer::parseInt).collect(toSet());
	}

	public Map<String, String> getNames(String origin, int id, boolean defaultNames) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(3);
		parameters.put("origin", origin);
		parameters.put("id", id);
		parameters.put("defaultNames", "1");

		Object response = request("names", parameters);
		return (Map) getMap(response, "data");
	}

	public Object request(String path, Map<String, Object> parameters) throws Exception {
		return request(path + '?' + encodeParameters(parameters, true));
	}

	public Object request(String path) throws Exception {
		return getCache().json(path, this::getResource).expire(Cache.ONE_WEEK).get();
	}

	protected URL getResource(String path) throws Exception {
		return new URL("http://thexem.de/map/" + path);
	}

	protected Cache getCache() {
		return Cache.getCache("xem", CacheType.Monthly);
	}

}
