package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.util.RegularExpressions.*;
import static net.filebot.util.StringUtilities.*;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.Icon;
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
import net.filebot.WebServices;

public class AnimeLists implements Datasource {

	@Override
	public String getIdentifier() {
		return "AnimeLists";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	public Optional<Episode> map(Episode episode, DB source, DB destination) throws Exception {
		int id = episode.getSeriesInfo().getId();
		int series = getSeasonNumber(source, episode);

		return find(source, id, series).map(a -> {
			// auto-align mode
			if (a.defaulttvdbseason == null) {
				try {
					return mapAutoAligned(destination, a, episode);
				} catch (Exception e) {
					debug.warning(e::toString);
				}
				return null;
			}

			// offset mode
			int s = getSeasonNumber(source, episode);
			int e = episode.isSpecial() ? episode.getSpecial() : episode.getEpisode();

			// check explicit episode mapping
			if (a.mapping != null) {
				for (Mapping m : a.mapping) {
					if (s == getSeason(source, m)) {
						Optional<Integer> episodeMapping = getEpisodeNumber(source, m, e);
						if (episodeMapping.isPresent()) {
							return derive(destination, a, episode, getSeason(destination, m), episodeMapping.get());
						}
					}
				}
			}

			// apply default season
			s = getSeason(destination, a, episode);

			// apply episode offset
			e += getEpisodeNumberOffset(destination, a);

			return derive(destination, a, episode, s, e);
		}).findFirst();
	}

	protected Episode derive(DB db, Entry a, Episode episode, int s, int e) {
		if (s == 0) {
			// special
			return episode.derive(getSeriesName(db, a), null, null, null, e);
		} else {
			// regular
			return episode.derive(getSeriesName(db, a), null, e, null, null);
		}
	}

	public Optional<Integer> map(int id, int s, DB source, DB destination) throws Exception {
		return find(source, id, s).map(a -> getId(destination, a)).findFirst();
	}

	protected Episode mapAutoAligned(DB db, Entry a, Episode episode) throws Exception {
		switch (db) {
		case AniDB:
			return WebServices.AniDB.getEpisodeList(a.anidbid, SortOrder.Absolute, Locale.ENGLISH).stream().filter(e -> {
				return episode.getAbsolute() != null && episode.getAbsolute().equals(e.getEpisode());
			}).findFirst().orElse(null);
		default:
			return WebServices.TheTVDB.getEpisodeList(a.tvdbid, SortOrder.Airdate, Locale.ENGLISH).stream().filter(e -> {
				return episode.getEpisode() != null && episode.getEpisode().equals(e.getAbsolute());
			}).findFirst().orElse(null);
		}
	}

	protected Optional<Integer> getEpisodeNumber(DB db, Mapping m, Integer e) {
		if (m.numbers != null) {
			switch (db) {
			case AniDB:
				return stream(m.numbers).filter(i -> e == i[0]).map(i -> i[1]).findFirst();
			default:
				return stream(m.numbers).filter(i -> e == i[1]).map(i -> i[0]).findFirst();
			}
		}
		return Optional.empty();
	}

	protected int getEpisodeNumberOffset(DB db, Entry a) {
		if (a.episodeoffset == null) {
			return 0;
		}

		switch (db) {
		case AniDB:
			return -a.episodeoffset;
		default:
			return a.episodeoffset;
		}
	}

	protected int getSeasonNumber(DB db, Episode e) {
		// special episode
		if (e.isSpecial()) {
			return 0;
		}

		// regular absolute episode
		if (e.getSeason() == null) {
			switch (db) {
			case AniDB:
				return 1;
			default:
				return -1;
			}
		}

		// regular SxE episode
		return e.getSeason();
	}

	protected int getSeason(DB db, Mapping m) {
		switch (db) {
		case AniDB:
			return m.anidbseason;
		default:
			return m.tvdbseason;
		}
	}

	protected int getSeason(DB db, Entry a, Episode e) {
		if (e.isSpecial()) {
			return 0;
		}

		switch (db) {
		case AniDB:
			return 1;
		default:
			return a.defaulttvdbseason;
		}
	}

	protected int getId(DB db, Entry a) {
		switch (db) {
		case AniDB:
			return a.anidbid;
		default:
			return a.tvdbid;
		}
	}

	protected String getSeriesName(DB db, Entry a) {
		switch (db) {
		case AniDB:
			return a.name;
		default:
			return a.tvdbname;
		}
	}

	protected boolean isValid(Entry a) {
		return a.anidbid != null && a.tvdbid != null;
	}

	public Stream<Entry> find(DB db, int id) throws Exception {
		return stream(getModel().anime).filter(this::isValid).filter(a -> id == getId(db, a));
	}

	public Stream<Entry> find(DB db, int id, int s) throws Exception {
		switch (db) {
		case AniDB:
			return find(db, id);
		default:
			return find(db, id).filter(a -> a.defaulttvdbseason == null || s == a.defaulttvdbseason);
		}
	}

	public Cache getCache() {
		return Cache.getCache(getIdentifier(), CacheType.Monthly);
	}

	public Model getModel() throws Exception {
		return getCache().bytes("https://github.com/ScudLee/anime-lists/raw/master/anime-list.xml", URL::new).transform(bytes -> {
			return unmarshal(bytes, Model.class);
		}).get();
	}

	public static DB getDB(Episode e) {
		return DB.forName(e.getSeriesInfo().getDatabase());
	}

	public static DB getDB(String s) {
		return DB.forName(s);
	}

	public enum DB {

		AniDB, TheTVDB;

		public static List<String> names() {
			return stream(values()).map(Enum::name).collect(toList());
		}

		public static DB forName(String name) {
			for (DB db : values()) {
				if (db.name().equalsIgnoreCase(name)) {
					return db;
				}
			}
			throw new IllegalArgumentException(String.format("%s not in %s", name, asList(values())));
		}
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

		@XmlElement
		public String tvdbname;

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
		public int[][] numbers;

		@Override
		public String toString() {
			return marshal(this, Mapping.class);
		}
	}

	protected static class NumberAdapter extends XmlAdapter<String, Integer> {

		@Override
		public Integer unmarshal(String s) throws Exception {
			return matchInteger(s);
		}

		@Override
		public String marshal(Integer i) throws Exception {
			return i == null ? null : Integer.toString(i);
		}
	}

	protected static class NumberMapAdapter extends XmlAdapter<String, int[][]> {

		@Override
		public int[][] unmarshal(String s) throws Exception {
			return tokenize(s, SEMICOLON).map(m -> matchIntegers(m)).filter(m -> m.size() == 2).map(m -> m.stream().mapToInt(i -> i).toArray()).toArray(int[][]::new);
		}

		@Override
		public String marshal(int[][] m) throws Exception {
			return stream(m).map(e -> join(IntStream.of(e).boxed(), "-")).collect(joining(";"));
		}
	}

	public static <T> T unmarshal(byte[] bytes, Class<T> type) {
		try {
			return (T) JAXBContext.newInstance(type).createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T> String marshal(T object, Class<T> type) {
		try {
			StringWriter buffer = new StringWriter();
			Marshaller marshaller = JAXBContext.newInstance(type).createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.marshal(object, buffer);
			return buffer.toString();
		} catch (Exception e) {
			return e.toString();
		}
	}

}
