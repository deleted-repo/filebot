package net.filebot.ui;

import static com.bulenkov.iconloader.util.ColorUtil.*;
import static net.filebot.Logging.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.util.logging.Level;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.bulenkov.darcula.DarculaLaf;

import net.filebot.util.ui.GradientStyle;
import net.filebot.util.ui.notification.SeparatorBorder;
import net.filebot.util.ui.notification.SeparatorBorder.Position;

public class ThemeSupport {

	public static Color getPanelBackground() {
		return getColor(0xFFFFFF);
	}

	public static LinearGradientPaint getPanelBackgroundGradient(int x, int y, int w, int h) {
		float[] gradientFractions = { 0.0f, 0.5f, 1.0f };
		Color[] gradientColors = { getColor(0xF6F6F6), getColor(0xF8F8F8), getColor(0xF3F3F3) };

		return new LinearGradientPaint(x, y, w, h, gradientFractions, gradientColors);
	}

	public static SeparatorBorder getSeparatorBorder(Position position) {
		return new SeparatorBorder(1, getColor(0xB4B4B4), getColor(0xACACAC), GradientStyle.LEFT_TO_RIGHT, position);
	}

	public static Color getColor(int rgba) {
		return theme.getColor(rgba);
	}

	private static Theme theme = Theme.System;

	public static void setTheme(Theme t) {
		theme = t;

		try {
			theme.setLookAndFeel();
		} catch (Exception e) {
			log.log(Level.SEVERE, e, message("Failed to set LaF", t));
		}
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
				return isDark(c) ? c : shift(c, 0.2);
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
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel());
			}
		};

		public Color getColor(int rgba) {
			return new Color(rgba);
		}

		public abstract void setLookAndFeel() throws Exception;
	}

}
