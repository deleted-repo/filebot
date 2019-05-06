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

import net.filebot.Cache;
import net.filebot.CacheType;

public class XEM {

	public Object[] getAll(Integer id, String origin) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(2);
		parameters.put("id", id);
		parameters.put("origin", origin);

		Object response = request("all?" + encodeParameters(parameters, true));

		return getArray(response, "data");
	}

	public List<SearchResult> getAllNames(String origin) throws Exception {
		return getAllNames(origin, null, null, true);
	}

	public List<SearchResult> getAllNames(String origin, String season, String language, boolean defaultNames) throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("origin", origin);
		parameters.put("season", season);
		parameters.put("language", language);
		parameters.put("defaultNames", defaultNames ? "1" : "0");

		Object response = request("allNames?" + encodeParameters(parameters, true));

		List<SearchResult> result = new ArrayList<>();
		getMap(response, "data").forEach((k, v) -> {
			int id = Integer.parseInt(k.toString());
			List<String> names = stream(asArray(v)).filter(Objects::nonNull).map(Objects::toString).filter(s -> s.length() > 0).collect(toList());

			if (names.size() > 0) {
				result.add(new SearchResult(id, names.get(0), names.subList(1, names.size())));
			}
		});
		return result;

	}

	public Object request(String path) throws Exception {
		return getCache().json(path, this::getResource).get();
	}

	protected URL getResource(String path) throws Exception {
		return new URL("http://thexem.de/map/" + path);
	}

	protected Cache getCache() {
		return Cache.getCache("xem", CacheType.Monthly);
	}

	// TODO REMOVE
	public static void main(String[] args) throws Exception {
		XEM xem = new XEM();
		System.out.println(xem.getAllNames("tvdb"));

		for (Object i : xem.getAll(13033, "anidb")) {
			System.out.println(i);
		}

		System.exit(0);
	}

}
