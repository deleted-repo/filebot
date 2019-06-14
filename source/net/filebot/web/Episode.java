package net.filebot.web;

import static net.filebot.WebServices.*;
import static net.filebot.web.EpisodeUtilities.*;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Episode implements Serializable {

	// base episode record
	protected String seriesName;
	protected Integer season;
	protected Integer episode;
	protected String title;

	// absolute episode number
	protected Integer absolute;

	// special number
	protected Integer special;

	// episode airdate
	protected SimpleDate airdate;

	// unique identifier
	protected Integer id;

	// extended series metadata
	protected SeriesInfo seriesInfo;

	public Episode() {
		// used by deserializer
	}

	public Episode(Episode obj) {
		this(obj.seriesName, obj.season, obj.episode, obj.title, obj.absolute, obj.special, obj.airdate, obj.id, obj.seriesInfo);
	}

	public Episode(String seriesName, Integer season, Integer episode, String title) {
		this(seriesName, season, episode, title, null, null, null, null, null);
	}

	public Episode(String seriesName, Integer season, Integer episode, String title, Integer absolute, Integer special, SimpleDate airdate, Integer id, SeriesInfo seriesInfo) {
		this.seriesName = seriesName;
		this.season = season;
		this.episode = episode;
		this.title = title;
		this.absolute = absolute;
		this.special = special;
		this.airdate = airdate == null ? null : airdate.clone();
		this.id = id;
		this.seriesInfo = seriesInfo == null ? null : seriesInfo.clone();
	}

	public String getSeriesName() {
		return seriesName;
	}

	public Integer getEpisode() {
		return episode;
	}

	public Integer getSeason() {
		return season;
	}

	public String getTitle() {
		return title;
	}

	public Integer getAbsolute() {
		return absolute;
	}

	public Integer getSpecial() {
		return special;
	}

	public SimpleDate getAirdate() {
		return airdate;
	}

	public Integer getId() {
		return id;
	}

	public SeriesInfo getSeriesInfo() {
		return seriesInfo;
	}

	public Set<String> getSeriesNames() {
		Set<String> names = new LinkedHashSet<String>();
		if (seriesName != null) {
			names.add(seriesName);
		}
		if (seriesInfo != null) {
			if (seriesInfo.name != null) {
				names.add(seriesInfo.name);
			}
			if (seriesInfo.aliasNames != null) {
				for (String it : seriesInfo.aliasNames) {
					names.add(it);
				}
			}
		}
		return names;
	}

	public boolean isAnime() {
		return seriesInfo != null && (Objects.equals(seriesInfo.getType(), SeriesInfo.TYPE_ANIME) || isInstance(AniDB, seriesInfo)); // HACK: check database == AniDB for backward compatibility
	}

	public boolean isRegular() {
		return episode != null;
	}

	public boolean isSpecial() {
		return special != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Episode) {
			Episode other = (Episode) obj;

			// check if database id matches
			if (id != null && other.id != null) {
				return id.equals(other.id) && Objects.equals(seriesInfo, other.seriesInfo);
			}

			// check if episode record matches
			return Objects.equals(season, other.season) && Objects.equals(episode, other.episode) && Objects.equals(absolute, other.absolute) && Objects.equals(special, other.special) && Objects.equals(seriesName, other.seriesName) && Objects.equals(title, other.title);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return id != null ? id : Objects.hash(season, episode, absolute, special, seriesName, title);
	}

	@Override
	public Episode clone() {
		return new Episode(this);
	}

	public Episode derive(Integer season, Integer episode) {
		return derive(getSeriesName(), season, episode, getAbsolute(), null);
	}

	public Episode deriveSpecial(Integer special) {
		return derive(getSeriesName(), getSeason(), null, getAbsolute(), special);
	}

	public Episode derive(String seriesName, Integer season, Integer episode, Integer absolute, Integer special) {
		return new Episode(seriesName, season, episode, null, absolute, special, null, null, null);
	}

	@Override
	public String toString() {
		return EpisodeFormat.SeasonEpisode.format(this);
	}

}
