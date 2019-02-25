package net.filebot.ui;

import static java.util.Arrays.*;
import static javax.swing.BorderFactory.*;
import static net.filebot.Logging.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.util.logging.Level;

import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.bulenkov.darcula.DarculaLaf;
import com.bulenkov.iconloader.util.ColorUtil;

import net.filebot.util.SystemProperty;
import net.filebot.util.ui.GradientStyle;
import net.filebot.util.ui.notification.SeparatorBorder;
import net.filebot.util.ui.notification.SeparatorBorder.Position;

public class ThemeSupport {

	private static Theme theme = SystemProperty.of("net.filebot.theme", Theme::forName, Theme.System).get();

	public static Theme getTheme() {
		return theme;
	}

	public static void setTheme() {
		setTheme(theme);
	}

	public static void setTheme(Theme t) {
		try {
			theme = t;
			theme.setLookAndFeel();
		} catch (Exception e) {
			log.log(Level.SEVERE, e, message("Failed to set LaF", t));
		}
	}

	public static Color getColor(int rgba) {
		return theme.getColor(rgba);
	}

	public static Color getPanelBackground() {
		return getColor(0xFFFFFF);
	}

	public static Color getLabelForeground() {
		return getColor(0x101010);
	}

	public static Color getHelpPanelBackground() {
		return getColor(0xFFFFE1);
	}

	public static Border getHelpPanelBorder() {
		return createLineBorder(getColor(0xACA899));
	}

	public static LinearGradientPaint getPanelBackgroundGradient(int x, int y, int w, int h) {
		float[] gradientFractions = { 0.0f, 0.5f, 1.0f };
		Color[] gradientColors = { getColor(0xF6F6F6), getColor(0xF8F8F8), getColor(0xF3F3F3) };

		return new LinearGradientPaint(x, y, w, h, gradientFractions, gradientColors);
	}

	public static SeparatorBorder getSeparatorBorder(Position position) {
		return new SeparatorBorder(1, getColor(0xB4B4B4), getColor(0xACACAC), GradientStyle.LEFT_TO_RIGHT, position);
	}

	public enum Theme {

		System {

			@Override
			public void setLookAndFeel() throws Exception {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		},

		CrossPlatform {

			@Override
			public void setLookAndFeel() throws Exception {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
		},

		Darcula {

			@Override
			public void setLookAndFeel() throws Exception {
				UIManager.setLookAndFeel(new DarculaLaf());
			}

			@Override
			public Color getColor(int rgba) {
				return getDarkColor(new Color(rgba));
			}

			public Color getDarkColor(Color c) {
				return ColorUtil.shift(c, ColorUtil.isDark(c) ? 9 : 0.2);
			}

			@Override
			public boolean isDark() {
				return true;
			}
		},

		Nimbus {

			@Override
			public void setLookAndFeel() throws Exception {
				UIManager.setLookAndFeel(new NimbusLookAndFeel());
			}
		},

		Metal {

			@Override
			public void setLookAndFeel() throws Exception {
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			}
		};

		public Color getColor(int rgba) {
			return new Color(rgba);
		}

		public boolean isDark() {
			return false;
		}

		public abstract void setLookAndFeel() throws Exception;

		public static Theme forName(String name) {
			for (Theme t : values()) {
				if (t.name().equalsIgnoreCase(name)) {
					return t;
				}
			}
			throw new IllegalArgumentException(String.format("%s not in %s", name, asList(values())));
		}
	}

}
