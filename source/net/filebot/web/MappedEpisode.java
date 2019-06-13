package net.filebot.web;

import java.util.Set;
import java.util.function.Function;

public class MappedEpisode extends Episode {

	protected Episode mapping;

	public MappedEpisode() {
		// used by deserializer
	}

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

	private <T> T getFirst(Function<Episode, T> getter) {
		T mappingValue = getter.apply(mapping);
		return mappingValue != null ? mappingValue : getter.apply(getOriginal());
	}

	@Override
	public String getSeriesName() {
		return getFirst(Episode::getSeriesName);
	}

	@Override
	public Integer getEpisode() {
		return mapping.getEpisode(); // always use mapped episode number
	}

	@Override
	public Integer getSeason() {
		return mapping.getSeason(); // always use mapped season number
	}

	@Override
	public String getTitle() {
		return getFirst(Episode::getTitle);
	}

	@Override
	public Integer getAbsolute() {
		return getFirst(Episode::getAbsolute);
	}

	@Override
	public Integer getSpecial() {
		return mapping.getSpecial(); // always use mapped special number
	}

	@Override
	public SimpleDate getAirdate() {
		return getFirst(Episode::getAirdate);
	}

	@Override
	public Integer getId() {
		return getFirst(Episode::getId);
	}

	@Override
	public Set<String> getSeriesNames() {
		return getFirst(Episode::getSeriesNames);
	}

	@Override
	public boolean isAnime() {
		return super.isAnime(); // series info is only stored in the original episode object
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
	public SeriesInfo getSeriesInfo() {
		return super.getSeriesInfo(); // series info is only stored in the original episode object
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj); // use original episode object for episode comparison
	}

	@Override
	public int hashCode() {
		return super.hashCode(); // use original episode object for episode comparison
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
		return String.format("%s [%s]", getMapping(), getOriginal());
	}

}
