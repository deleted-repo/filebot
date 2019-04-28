package net.filebot.web;

import java.io.Serializable;

public class TheTVDBSearchResult extends SearchResult implements Serializable {

	protected String slug;
	protected SimpleDate firstAired;
	protected String overview;
	protected String network;
	protected String status;

	public TheTVDBSearchResult(int id, String name, String[] aliasNames, String slug, SimpleDate firstAired, String overview, String network, String status) {
		super(id, name, aliasNames);
		this.slug = slug;
		this.firstAired = firstAired;
		this.overview = overview;
		this.network = network;
		this.status = status;
	}

	public String getSlug() {
		return slug;
	}

	public SimpleDate getFirstAired() {
		return firstAired;
	}

	public String getOverview() {
		return overview;
	}

	public String getNetwork() {
		return network;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public SearchResult clone() {
		return new TheTVDBSearchResult(id, name, aliasNames, slug, firstAired, overview, network, status);
	}

}
