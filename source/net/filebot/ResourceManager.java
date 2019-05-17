package net.filebot;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

public final class ResourceManager {

	private static final Map<String, Icon> cache = synchronizedMap(new HashMap<String, Icon>(256));

	public static Icon getIcon(String name) {
		return cache.computeIfAbsent(name, i -> {
			// load image
			URL[] resource = getMultiResolutionImageResource(i);
			if (resource.length > 0) {
				return new ImageIcon(getMultiResolutionImage(resource));
			}

			// default image
			return null;
		});
	}

	public static Stream<URL> getApplicationIconResources() {
		return Stream.of("window.icon16", "window.icon64").map(ResourceManager::getImageResource);
	}

	public static List<Image> getApplicationIconImages() {
		return Stream.of("window.icon16", "window.icon64").map(ResourceManager::getMultiResolutionImageResource).map(ResourceManager::getMultiResolutionImage).collect(toList());
	}

	public static Icon getFlagIcon(String languageCode) {
		return getIcon("flags/" + languageCode);
	}

	private static URL[] getMultiResolutionImageResource(String name) {
		return Stream.of(name, name + "@2x").map(ResourceManager::getImageResource).filter(Objects::nonNull).toArray(URL[]::new);
	}

	private static URL getImageResource(String name) {
		return ResourceManager.class.getResource("resources/" + name + ".png");
	}

	private static Image getMultiResolutionImage(URL[] resource) {
		try {
			// Load multi-resolution images only if necessary
			if (PRIMARY_SCALE_FACTOR == 1) {
				return ImageIO.read(resource[0]);
			}

			List<BufferedImage> image = new ArrayList<BufferedImage>(resource.length);
			for (URL r : resource) {
				image.add(ImageIO.read(r));
			}

			// Windows 10: use down-scaled @2x image for non-integer scale factors 1.25 / 1.5 / 1.75
			if (PRIMARY_SCALE_FACTOR > 1 && PRIMARY_SCALE_FACTOR < 2 && image.size() > 1) {
				image.add(1, scale(PRIMARY_SCALE_FACTOR / 2, image.get(1)));
			} else if (PRIMARY_SCALE_FACTOR > 2) {
				image.add(scale(PRIMARY_SCALE_FACTOR / 2, image.get(1)));
			}

			return new BaseMultiResolutionImage(image.toArray(Image[]::new));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Image getMultiResolutionImage(BufferedImage baseImage, double baseScale) {
		if (PRIMARY_SCALE_FACTOR == 1 && baseScale == 1) {
			return baseImage;
		}

		List<BufferedImage> image = new ArrayList<BufferedImage>(3);
		image.add(baseImage);

		// use down-scaled @2x image as @1x base image
		if (baseScale > 1) {
			image.add(0, scale(1 / baseScale, baseImage));
		}

		// Windows 10: use down-scaled @2x image for non-integer scale factors 1.25 / 1.5 / 1.75
		if (PRIMARY_SCALE_FACTOR > 1 && PRIMARY_SCALE_FACTOR < baseScale) {
			image.add(1, scale(PRIMARY_SCALE_FACTOR / baseScale, baseImage));
		} else if (PRIMARY_SCALE_FACTOR > baseScale) {
			image.add(scale(PRIMARY_SCALE_FACTOR / baseScale, baseImage));
		}

		return new BaseMultiResolutionImage(image.toArray(Image[]::new));
	}

	private static final double PRIMARY_SCALE_FACTOR = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();

	private static BufferedImage scale(double scale, BufferedImage image) {
		int w = (int) (scale * image.getWidth());
		int h = (int) (scale * image.getHeight());
		return Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_TO_WIDTH, w, h, Scalr.OP_ANTIALIAS);
	}

}
