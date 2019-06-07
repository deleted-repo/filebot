package net.filebot.web;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.WebServices.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public final class EpisodeUtilities {

	public static Episode mapEpisode(Episode episode, Function<Episode, Episode> mapper) {
		return createEpisode(getMultiEpisodeList(episode).stream().map(mapper).sorted(EPISODE_NUMBERS_COMPARATOR).collect(toList()));
	}

	public static Episode selectEpisode(List<Episode> episodelist, Episode selection) {
		return createEpisode(episodelist.stream().filter(getMultiEpisodeList(selection)::contains).sorted(EPISODE_NUMBERS_COMPARATOR).collect(toList()));
	}

	private static Episode createEpisode(List<Episode> episode) {
		if (episode.isEmpty()) {
			throw new IllegalArgumentException("Invalid Episode: Empty");
		}
		return episode.size() == 1 ? episode.get(0) : new MultiEpisode(episode);
	}

	public static List<Episode> getMultiEpisodeList(Episode e) {
		return e instanceof MultiEpisode ? ((MultiEpisode) e).getEpisodes() : singletonList(e);
	}

	public static boolean isAnime(Episode e) {
		return AniDB.getIdentifier().equals(e.getSeriesInfo().getDatabase());
	}

	public static boolean isRegular(Episode e) {
		return e.getEpisode() != null && e.getSpecial() == null;
	}

	public static List<Episode> fetchEpisodeList(Episode episode) throws Exception {
		return fetchEpisodeList(episode, null, null);
	}

	public static List<Episode> fetchEpisodeList(Episode episode, SortOrder preferredSortOrder, Locale preferredLocale) throws Exception {
		SeriesInfo info = episode.getSeriesInfo();

		SortOrder order = Optional.ofNullable(preferredSortOrder).orElseGet(() -> SortOrder.valueOf(info.getOrder())); // default to original order
		Locale locale = Optional.ofNullable(preferredLocale).orElseGet(() -> new Locale(info.getLanguage())); // default to original locale

		return getEpisodeListProvider(info.getDatabase()).getEpisodeList(info.getId(), order, locale);
	}

	public static Episode fetchEpisode(Episode episode, SortOrder preferredSortOrder, Locale preferredLocale) throws Exception {
		return selectEpisode(fetchEpisodeList(episode, preferredSortOrder, preferredLocale), episode);
	}

	public static Episode trySeasonEpisodeForAnime(Episode episode) {
		if (isAnime(episode) && isRegular(episode)) {
			return mapEpisode(episode, e -> {
				try {
					return AnimeLists.forName(e.getSeriesInfo().getDatabase()).map(e, AnimeLists.TheTVDB).orElse(e);
				} catch (Exception ioe) {
					debug.warning(ioe::toString);
					return e;
				}
			});
		}
		return episode;
	}

	public static List<Episode> filterBySeason(Collection<Episode> episodes, int season) {
		return episodes.stream().filter(it -> {
			return it.getSeason() != null && season == it.getSeason();
		}).collect(toList());
	}

	public static int getLastSeason(Collection<Episode> episodes) {
		return episodes.stream().mapToInt(it -> {
			return it.getSeason() == null ? 0 : it.getSeason();
		}).max().orElse(0);
	}

	public static Comparator<Episode> episodeComparator() {
		return EPISODE_NUMBERS_COMPARATOR;
	}

	public static final Comparator<Episode> EPISODE_NUMBERS_COMPARATOR = new Comparator<Episode>() {

		@Override
		public int compare(Episode a, Episode b) {
			int diff = compareValue(a.getSeason(), b.getSeason());
			if (diff != 0)
				return diff;

			diff = compareValue(a.getEpisode(), b.getEpisode());
			if (diff != 0)
				return diff;

			diff = compareValue(a.getSpecial(), b.getSpecial());
			if (diff != 0)
				return diff;

			return compareValue(a.getAbsolute(), b.getAbsolute());
		}

		private <T> int compareValue(Comparable<T> o1, T o2) {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null && o2 != null)
				return Integer.MAX_VALUE;
			if (o1 != null && o2 == null)
				return Integer.MIN_VALUE;

			return o1.compareTo(o2);
		}
	};

	private EpisodeUtilities() {
		throw new UnsupportedOperationException();
	}

}
