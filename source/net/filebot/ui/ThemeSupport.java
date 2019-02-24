package net.filebot.ui;

import static com.bulenkov.iconloader.util.ColorUtil.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;

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
		if (dark) {
			return getDarkColor(new Color(rgba));
		}
		return new Color(rgba);
	}

	public static Color getDarkColor(Color c) {
		return isDark(c) ? c : shift(c, 0.2);
	}

	private static boolean dark = false;

}
