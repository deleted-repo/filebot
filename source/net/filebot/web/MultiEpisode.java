package net.filebot.web;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MultiEpisode extends Episode implements Iterable<Episode> {

	protected Episode[] episodes;

	public MultiEpisode() {
		// used by deserializer
	}

	public MultiEpisode(Episode... episodes) {
		this.episodes = episodes.clone();
	}

	public MultiEpisode(List<Episode> episodes) {
		this.episodes = episodes.toArray(new Episode[0]);
	}

	public Episode[] getEpisodes() {
		return episodes.clone();
	}

	public Episode getFirst() {
		return episodes[0];
	}

	public Stream<Episode> stream() {
		return Arrays.stream(episodes);
	}

	@Override
	public Iterator<Episode> iterator() {
		return stream().iterator();
	}

	@Override
	public String getSeriesName() {
		return getFirst().getSeriesName();
	}

	@Override
	public Integer getEpisode() {
		return getFirst().getEpisode();
	}

	@Override
	public Integer getSeason() {
		return getFirst().getSeason();
	}

	@Override
	public String getTitle() {
		return EpisodeFormat.SeasonEpisode.formatMultiTitle(episodes);
	}

	@Override
	public Integer getAbsolute() {
		return getFirst().getAbsolute();
	}

	@Override
	public Integer getSpecial() {
		return getFirst().getSpecial();
	}

	@Override
	public SimpleDate getAirdate() {
		return getFirst().getAirdate();
	}

	@Override
	public Integer getId() {
		return getFirst().getId();
	}

	@Override
	public SeriesInfo getSeriesInfo() {
		return getFirst().getSeriesInfo();
	}

	@Override
	public Set<String> getSeriesNames() {
		return getFirst().getSeriesNames();
	}

	@Override
	public boolean isAnime() {
		return getFirst().isAnime();
	}

	@Override
	public boolean isRegular() {
		return getFirst().isRegular();
	}

	@Override
	public boolean isSpecial() {
		return getFirst().isSpecial();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MultiEpisode) {
			MultiEpisode other = (MultiEpisode) obj;
			return Arrays.equals(episodes, other.episodes);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(episodes);
	}

	@Override
	public MultiEpisode clone() {
		return new MultiEpisode(episodes);
	}

	@Override
	public MultiEpisode derive(String seriesName, Integer season, Integer episode, Integer absolute, Integer special) {
		Episode[] m = new Episode[episodes.length];
		for (int i = 0; i < episodes.length; i++) {
			m[i] = episodes[i].derive(seriesName, season, up(episode, i), up(absolute, i), up(special, i));
		}
		return new MultiEpisode(m);
	}

	private Integer up(Integer i, int delta) {
		return i == null ? null : i + delta;
	}

	@Override
	public String toString() {
		return EpisodeFormat.SeasonEpisode.formatMultiEpisode(episodes);
	}

}
