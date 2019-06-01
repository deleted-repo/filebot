package net.filebot.ui;

import static java.util.Arrays.*;
import static javax.swing.BorderFactory.*;
import static net.filebot.Logging.*;

import java.awt.Color;
import java.awt.Insets;
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
import net.filebot.util.ui.ProgressIndicator;
import net.filebot.util.ui.RoundBorder;
import net.filebot.util.ui.SelectionPainter;
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
		return theme.isDark() ? new Color(0x313131) : new Color(0xFFFFE1);
	}

	public static Border getHelpPanelBorder() {
		return createLineBorder(getColor(0xACA899));
	}

	public static Color getErrorColor() {
		return Color.red;
	}

	public static Color getLinkColor() {
		return theme.getLinkSelectionForeground();
	}

	public static Color getActiveColor() {
		return new Color(0x6495ED);// Cornflower Blue
	}

	public static Color getPassiveColor() {
		return Color.lightGray;
	}

	public static Color getVerificationColor() {
		return new Color(0x009900);
	}

	public static Color getPanelSelectionBorderColor() {
		return theme.isDark() ? new Color(0x191c26) : new Color(0x163264);
	}

	public static Color getBlankBackgroundColor() {
		return getColor(0xF8F8FF);
	}

	public static LinearGradientPaint getPanelBackgroundGradient(int x, int y, int w, int h) {
		float[] gradientFractions = { 0.0f, 0.5f, 1.0f };
		Color[] gradientColors = { getColor(0xF6F6F6), getColor(0xF8F8F8), getColor(0xF3F3F3) };

		return new LinearGradientPaint(x, y, w, h, gradientFractions, gradientColors);
	}

	public static Border getRoundBorder() {
		return new RoundBorder(getColor(0xACACAC), 12, new Insets(1, 1, 1, 1));
	}

	public static Border getSeparatorBorder(Position position) {
		return new SeparatorBorder(1, getColor(0xB4B4B4), getColor(0xACACAC), GradientStyle.LEFT_TO_RIGHT, position);
	}

	public static Border getHorizontalRule() {
		return new SeparatorBorder(2, new Color(0, 0, 0, 90), GradientStyle.TOP_TO_BOTTOM, SeparatorBorder.Position.BOTTOM);
	}

	public static ProgressIndicator getProgressIndicator() {
		return new ProgressIndicator(Color.orange, withAlpha(getLabelForeground(), 0.25f));
	}

	public static Color withAlpha(Color color, float alpha) {
		return new Color(((int) ((alpha * 255)) << 24) | (color.getRGB() & 0x00FFFFFF), true);
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
				// Maybe fix NPE on Linux
				// @see https://github.com/bulenkov/iconloader/issues/14
				UIManager.getFont("Label.font");

				UIManager.setLookAndFeel(new DarculaLaf());

				Color selectionBackground = new Color(0x39698a);
				Color componentBackground = new Color(0x3A3D3E);

				UIManager.put("List.selectionBackground", selectionBackground);
				UIManager.put("ComboBox.selectionBackground", selectionBackground);
				UIManager.put("Table.selectionBackground", selectionBackground);
				UIManager.put("Menu.selectionBackground", selectionBackground);
				UIManager.put("MenuItem.selectionBackground", selectionBackground);
				UIManager.put("MenuItem.selectedBackgroundPainter", new SelectionPainter(selectionBackground));
				UIManager.put("PopupMenu.selectionBackground", selectionBackground);
				UIManager.put("Tree.selectionBackground", selectionBackground);
				UIManager.put("Tree.selectionInactiveBackground", selectionBackground);
				UIManager.put("Table.background", componentBackground);
				UIManager.put("TabbedPane.selected", componentBackground);
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

			@Override
			public Color getLinkSelectionForeground() {
				return new Color(0x5195C4);
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

		public Color getColor(int rgb) {
			return new Color(rgb);
		}

		public boolean isDark() {
			return false;
		}

		public Color getLinkSelectionForeground() {
			return new Color(0x3399FF);
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
