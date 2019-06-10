package net.filebot.web;

import java.util.Set;

public class MappedEpisode extends Episode {

	protected Episode mapping;

	public MappedEpisode(Episode original, Episode mapping) {
		super(original);
		this.mapping = mapping;
	}

	public Episode getOriginal() {
		return new Episode(this);
	}

	public Episode getMapping() {
		return mapping;
	}

	@Override
	public String getSeriesName() {
		return mapping.getSeriesName();
	}

	@Override
	public Integer getEpisode() {
		return mapping.getEpisode();
	}

	@Override
	public Integer getSeason() {
		return mapping.getSeason();
	}

	@Override
	public String getTitle() {
		return mapping.getTitle();
	}

	@Override
	public Integer getAbsolute() {
		return mapping.getAbsolute();
	}

	@Override
	public Integer getSpecial() {
		return mapping.getSpecial();
	}

	@Override
	public SimpleDate getAirdate() {
		return mapping.getAirdate();
	}

	@Override
	public Integer getId() {
		return mapping.getId();
	}

	@Override
	public SeriesInfo getSeriesInfo() {
		return mapping.getSeriesInfo();
	}

	@Override
	public Set<String> getSeriesNames() {
		return mapping.getSeriesNames();
	}

	@Override
	public boolean isAnime() {
		return mapping.isAnime();
	}

	@Override
	public boolean isRegular() {
		return mapping.isRegular();
	}

	@Override
	public boolean isSpecial() {
		return mapping.isSpecial();
	}

	@Override
	public boolean equals(Object obj) {
		return mapping.equals(obj);
	}

	@Override
	public int hashCode() {
		return mapping.hashCode();
	}

	@Override
	public MappedEpisode clone() {
		return new MappedEpisode(this, mapping);
	}

	@Override
	public MappedEpisode derive(Integer season, Integer episode) {
		return new MappedEpisode(this, mapping.derive(season, episode));
	}

	@Override
	public MappedEpisode deriveSpecial(Integer special) {
		return new MappedEpisode(this, mapping.deriveSpecial(special));
	}

	@Override
	public MappedEpisode derive(String seriesName, Integer season, Integer episode, Integer absolute, Integer special) {
		return new MappedEpisode(this, mapping.derive(seriesName, season, episode, absolute, special));
	}

	@Override
	public String toString() {
		return mapping.toString();
	}

}
