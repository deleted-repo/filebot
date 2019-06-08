package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.Logging.*;
import static net.filebot.util.RegularExpressions.*;
import static net.filebot.util.StringUtilities.*;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.filebot.Cache;
import net.filebot.CacheType;
import net.filebot.Resource;
import net.filebot.WebServices;

public enum AnimeLists {

	AniDB, TheTVDB;

	public Optional<Episode> map(Episode episode, AnimeLists destination) throws Exception {
		return find(episode.getSeriesInfo().getId()).map(a -> {
			// auto-align mode
			if (a.defaulttvdbseason == null) {
				try {
					return destination.mapAutoAligned(a, episode);
				} catch (Exception e) {
					debug.warning(e::toString);
				}
				return null;
			}

			// offset mode
			int s = getSeasonNumber(episode);
			int e = getEpisodeNumber(episode);

			// check explicit episode mapping
			if (a.mapping != null) {
				for (Mapping m : a.mapping) {
					if (s == getSeason(m)) {
						Optional<Integer> episodeMapping = destination.getEpisodeNumber(m, e);
						if (episodeMapping.isPresent()) {
							return derive(episode, destination.getSeason(m), episodeMapping.get());
						}
					}
				}
			}

			// apply default season
			s = destination.getSeason(a, episode);

			// apply episode offset
			e += destination.getEpisodeNumberOffset(a);

			return derive(episode, s, e);
		});
	}

	private Episode derive(Episode episode, int s, int e) {
		if (s == 0) {
			return episode.derive(null, null, e); // special episode
		} else {
			return episode.derive(s, e, null); // regular episode
		}
	}

	public Optional<Integer> map(int id, AnimeLists destination) throws Exception {
		return find(id).map(destination::getId);
	}

	protected Episode mapAutoAligned(Entry a, Episode episode) throws Exception {
		switch (this) {
		case TheTVDB:
			return WebServices.TheTVDB.getEpisodeList(a.tvdbid, SortOrder.Airdate, Locale.ENGLISH).stream().filter(e -> {
				return episode.getEpisode() != null && episode.getEpisode().equals(e.getAbsolute());
			}).findFirst().orElse(null);
		case AniDB:
			return WebServices.AniDB.getEpisodeList(a.anidbid, SortOrder.Absolute, Locale.ENGLISH).stream().filter(e -> {
				return episode.getAbsolute() != null && episode.getAbsolute().equals(e.getEpisode());
			}).findFirst().orElse(null);
		}
		return null;
	}

	protected Optional<Integer> getEpisodeNumber(Mapping m, Integer e) {
		if (m.numbers != null) {
			switch (this) {
			case TheTVDB:
				return Optional.ofNullable(m.numbers.get(e));
			case AniDB:
				return m.numbers.entrySet().stream().filter(it -> e.equals(it.getValue())).map(it -> it.getKey()).findFirst();
			}
		}
		return Optional.empty();
	}

	protected int getEpisodeNumberOffset(Entry a) {
		return a.episodeoffset == null ? 0 : this == TheTVDB ? a.episodeoffset : -a.episodeoffset;
	}

	protected int getSeasonNumber(Episode e) {
		// special episode
		if (e.getSpecial() != null) {
			return 0;
		}

		// regular absolute episode
		if (e.getSeason() == null) {
			return this == AniDB ? 1 : -1;
		}

		// regular SxE episode
		return e.getSeason();
	}

	protected int getEpisodeNumber(Episode e) {
		return e.getSpecial() != null ? e.getSpecial() : e.getEpisode();
	}

	protected int getSeason(Mapping m) {
		return this == AniDB ? m.anidbseason : m.tvdbseason;
	}

	protected int getSeason(Entry a, Episode e) {
		return e.getSpecial() != null ? 0 : this == AniDB ? 1 : a.defaulttvdbseason;
	}

	protected int getId(Entry a) {
		return this == AniDB ? a.anidbid : a.tvdbid;
	}

	protected boolean isValid(Entry a) {
		return a.anidbid != null && a.tvdbid != null;
	}

	public Optional<Entry> find(int id) throws Exception {
		return stream(MODEL.get().anime).filter(this::isValid).filter(a -> id == getId(a)).findFirst();
	}

	protected static Cache getCache() {
		return Cache.getCache("animelists", CacheType.Persistent);
	}

	protected static final Resource<Model> MODEL = Resource.lazy(() -> unmarshal(request("anime-list.xml"), Model.class));

	protected static byte[] request(String file) throws Exception {
		// NOTE: GitHub only supports If-None-Match (If-Modified-Since is ignored)
		Cache cache = getCache();

		return cache.bytes(file, AnimeLists::getResource).fetch(fetchIfNoneMatch(url -> file, cache)).expire(Cache.ONE_MONTH).get();
	}

	protected static URL getResource(String file) throws Exception {
		return new URL("https://raw.githubusercontent.com/ScudLee/anime-lists/master/" + file);
	}

	@XmlRootElement(name = "anime-list")
	public static class Model {

		@XmlElement
		public Entry[] anime;

		@Override
		public String toString() {
			return marshal(this, Model.class);
		}
	}

	@XmlRootElement(name = "anime")
	public static class Entry {

		@XmlAttribute
		public Integer anidbid;

		@XmlJavaTypeAdapter(NumberAdapter.class)
		@XmlAttribute
		public Integer tvdbid;

		@XmlJavaTypeAdapter(NumberAdapter.class)
		@XmlAttribute
		public Integer defaulttvdbseason;

		@XmlJavaTypeAdapter(NumberAdapter.class)
		@XmlAttribute
		public Integer episodeoffset;

		@XmlElement
		public String name;

		@XmlElementWrapper(name = "mapping-list")
		public Mapping[] mapping;

		@Override
		public String toString() {
			return marshal(this, Entry.class);
		}
	}

	@XmlRootElement(name = "mapping")
	public static class Mapping {

		@XmlAttribute
		public Integer anidbseason;

		@XmlAttribute
		public Integer tvdbseason;

		@XmlAttribute
		public Integer start;

		@XmlAttribute
		public Integer end;

		@XmlAttribute
		public Integer offset;

		@XmlJavaTypeAdapter(NumberMapAdapter.class)
		@XmlValue
		public Map<Integer, Integer> numbers;

		@Override
		public String toString() {
			return marshal(this, Mapping.class);
		}
	}

	private static <T> T unmarshal(byte[] bytes, Class<T> type) throws Exception {
		return (T) JAXBContext.newInstance(type).createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
	}

	private static <T> String marshal(T object, Class<T> type) {
		try {
			StringWriter buffer = new StringWriter();
			Marshaller marshaller = JAXBContext.newInstance(type).createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(object, buffer);
			return buffer.toString();
		} catch (Exception e) {
			return e.toString();
		}
	}

	private static class NumberAdapter extends XmlAdapter<String, Integer> {

		@Override
		public Integer unmarshal(String s) throws Exception {
			return matchInteger(s);
		}

		@Override
		public String marshal(Integer i) throws Exception {
			return String.valueOf(i);
		}
	}

	private static class NumberMapAdapter extends XmlAdapter<String, Map<Integer, Integer>> {

		@Override
		public Map<Integer, Integer> unmarshal(String s) throws Exception {
			return tokenize(s, SEMICOLON).map(m -> matchIntegers(m)).filter(m -> m.size() >= 2).collect(toMap(m -> m.get(0), m -> m.get(1)));
		}

		@Override
		public String marshal(Map<Integer, Integer> m) throws Exception {
			return m.entrySet().stream().map(e -> join(Stream.of(e.getKey(), e.getValue()), "-")).collect(joining(";"));
		}
	}

	public static List<String> names() {
		return stream(values()).map(Enum::name).collect(toList());
	}

	public static AnimeLists forName(String name) {
		for (AnimeLists db : values()) {
			if (db.name().equalsIgnoreCase(name)) {
				return db;
			}
		}

		throw new IllegalArgumentException(String.format("%s not in %s", name, asList(values())));
	}

	public static void main(String[] args) throws Exception {
		System.out.println(AnimeLists.AniDB.map(9183, AnimeLists.TheTVDB));

		List<Episode> episodes = WebServices.AniDB.getEpisodeList(9183, SortOrder.Absolute, Locale.ENGLISH);
		for (Episode episode : episodes) {
			System.out.println("\n" + episode);
			System.out.println(AnimeLists.AniDB.map(episode, AnimeLists.TheTVDB).get());
		}

		System.out.println("----------------------------");

		List<Episode> episodes2 = WebServices.TheTVDB.getEpisodeList(102261, SortOrder.Airdate, Locale.ENGLISH);
		for (Episode episode : episodes2) {
			System.out.println("\n" + episode);
			System.out.println(AnimeLists.TheTVDB.map(episode, AnimeLists.AniDB).get());
		}

		System.exit(0);
	}

}
