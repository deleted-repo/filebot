package net.filebot.web;

import static net.filebot.CachedResource.*;
import static net.filebot.util.StringUtilities.*;

import java.io.ByteArrayInputStream;
import java.net.URL;

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

public enum AnimeLists {

	AniDB, TheTVDB;

	public static final Resource<Model> MODEL = Resource.lazy(AnimeLists::fetchModel);

	protected static Cache getCache() {
		return Cache.getCache("animelists", CacheType.Monthly);
	}

	protected static Model fetchModel() throws Exception {
		Cache cache = getCache();

		// NOTE: GitHub only supports If-None-Match (If-Modified-Since is ignored)
		byte[] xml = cache.bytes("anime-list.xml", r -> {
			return new URL("https://raw.githubusercontent.com/ScudLee/anime-lists/master/" + r);
		}).fetch(fetchIfNoneMatch(URL::getFile, cache)).expire(Cache.ONE_MONTH).get();

		return (Model) JAXBContext.newInstance(Model.class).createUnmarshaller().unmarshal(new ByteArrayInputStream(xml));
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

	public static void main(String[] args) throws Exception {
		System.out.println(AnimeLists.MODEL.get());
		System.exit(0);
	}

}
