package net.filebot.web;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

public interface ThumbnailProvider {

	Map<SearchResult, Icon> getThumbnails(List<SearchResult> keys, ResolutionVariant variant) throws Exception;

	public static enum ResolutionVariant {

		NORMAL(1), RETINA(2);

		public final int scaleFactor;

		private ResolutionVariant(int scaleFactor) {
			this.scaleFactor = scaleFactor;
		}

		public static ResolutionVariant fromScaleFactor(Component parent) {
			return parent.getGraphicsConfiguration().getDefaultTransform().getScaleX() > 1 ? ResolutionVariant.RETINA : ResolutionVariant.NORMAL;
		}
	}

}
