package net.filebot.web;

import java.net.URL;

public class TheTVDBSearchResult extends SearchResult {

	protected String slug;
	protected SimpleDate firstAired;
	protected String overview;
	protected String network;
	protected String status;
	protected URL banner;

	public TheTVDBSearchResult(int id, String name, String[] aliasNames, String slug, SimpleDate firstAired, String overview, String network, String status, URL banner) {
		super(id, name, aliasNames);
		this.slug = slug;
		this.firstAired = firstAired;
		this.overview = overview;
		this.network = network;
		this.status = status;
		this.banner = banner;
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

	public URL getBanner() {
		return banner;
	}

}
