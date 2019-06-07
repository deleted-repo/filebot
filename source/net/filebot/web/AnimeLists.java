package net.filebot.web;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.CachedResource.*;
import static net.filebot.Logging.*;
import static net.filebot.util.StringUtilities.*;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
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
			if (destination == TheTVDB && a.defaulttvdbseason == null) {
				// auto-align mode
				try {
					return WebServices.TheTVDB.getEpisodeList(a.tvdbid, SortOrder.Airdate, Locale.ENGLISH).stream().filter(e -> {
						return episode.getEpisode() != null && episode.getEpisode().equals(e.getAbsolute());
					}).findFirst().orElse(null);
				} catch (Exception e) {
					debug.warning(e::toString);
					return null;
				}
			} else {
				// offset mode
				Integer s = destination == TheTVDB ? a.defaulttvdbseason : null;
				Integer e = episode.getEpisode();

				if (a.episodeoffset != null) {
					e = destination == TheTVDB ? e + a.episodeoffset : e - a.episodeoffset;
				}

				return episode.derive(s, e);
			}
		});
	}

	public Optional<Integer> map(int id, AnimeLists destination) throws Exception {
		return find(id).map(destination::getId);
	}

	public Optional<Entry> find(int id) throws Exception {
		return stream(MODEL.get().anime).filter(this::isValid).filter(a -> id == getId(a)).findFirst();
	}

	protected int getId(Entry a) {
		return this == AniDB ? a.anidbid : a.tvdbid;
	}

	protected boolean isValid(Entry a) {
		return a.anidbid != null && a.tvdbid != null;
	}

	protected static Cache getCache() {
		return Cache.getCache("animelists", CacheType.Persistent);
	}

	protected static final Resource<Model> MODEL = Resource.lazy(AnimeLists::fetchModel);

	protected static Model fetchModel() throws Exception {
		return (Model) JAXBContext.newInstance(Model.class).createUnmarshaller().unmarshal(new ByteArrayInputStream(request("anime-list.xml")));
	}

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
	}

	public static class Entry {

		@XmlAttribute
		public Integer anidbid;

		@XmlJavaTypeAdapter(OptionalIntegerAdapter.class)
		@XmlAttribute
		public Integer tvdbid;

		@XmlJavaTypeAdapter(OptionalIntegerAdapter.class)
		@XmlAttribute
		public Integer defaulttvdbseason;

		@XmlJavaTypeAdapter(OptionalIntegerAdapter.class)
		@XmlAttribute
		public Integer episodeoffset;

		@XmlElementWrapper(name = "mapping-list")
		public Mapping[] mapping;
	}

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

		@XmlValue
		public String value;
	}

	private static class OptionalIntegerAdapter extends XmlAdapter<String, Integer> {

		@Override
		public Integer unmarshal(String s) throws Exception {
			return matchInteger(s);
		}

		@Override
		public String marshal(Integer i) throws Exception {
			return String.valueOf(i);
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
		System.out.println(AnimeLists.AniDB.map(14444, AnimeLists.TheTVDB));

		List<Episode> episodes = WebServices.AniDB.getEpisodeList(14444, SortOrder.Absolute, Locale.ENGLISH);
		for (Episode episode : episodes) {
			System.out.println(AnimeLists.AniDB.map(episode, AnimeLists.TheTVDB));
		}

		System.exit(0);
	}

}
