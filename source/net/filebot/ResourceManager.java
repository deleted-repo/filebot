package net.filebot;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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

import net.filebot.util.SystemProperty;

public final class ResourceManager {

	private static final Map<String, Icon> cache = synchronizedMap(new HashMap<String, Icon>(256));

	public static Icon getIcon(String name) {
		return cache.computeIfAbsent(name, i -> {
			// load image
			URL[] resource = getMultiResolutionImageResource(i);
			if (resource.length > 0) {
				return getMultiResolutionIcon(resource);
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

	private static Image getMultiResolutionImage(URL[] resource) {
		try {
			List<BufferedImage> image = new ArrayList<BufferedImage>(resource.length);
			for (URL r : resource) {
				image.add(ImageIO.read(r));
			}
			return new BaseMultiResolutionImage(image.toArray(new Image[0]));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Icon getMultiResolutionIcon(URL[] resource) {
		if (PRIMARY_SCALE_FACTOR == 1 || PRIMARY_SCALE_FACTOR == 2) {
			return new ImageIcon(getMultiResolutionImage(resource));
		}

		try {
			BufferedImage[] image = new BufferedImage[resource.length + 1];
			for (int i = 0; i < resource.length; i++) {
				image[i + 1] = ImageIO.read(resource[i]);
			}
			image[0] = scale(PRIMARY_SCALE_FACTOR, image[image.length - 1]);
			return new ImageIcon(new BaseMultiResolutionImage(1, image));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static URL[] getMultiResolutionImageResource(String name) {
		return Stream.of(name, name + "@2x").map(ResourceManager::getImageResource).filter(Objects::nonNull).toArray(URL[]::new);
	}

	private static URL getImageResource(String name) {
		return ResourceManager.class.getResource("resources/" + name + ".png");
	}

	private static final float PRIMARY_SCALE_FACTOR = SystemProperty.of("sun.java2d.uiScale", Float::parseFloat, Toolkit.getDefaultToolkit().getScreenResolution() / 96f).get();

	private static BufferedImage scale(float scale, BufferedImage image) {
		int w = (int) (scale * image.getWidth());
		int h = (int) (scale * image.getHeight());

		BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = scaledImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(image, 0, 0, w, h, 0, 0, image.getWidth(), image.getHeight(), null);
		g2d.dispose();

		return scaledImage;
	}

	private ResourceManager() {
		throw new UnsupportedOperationException();
	}

}
