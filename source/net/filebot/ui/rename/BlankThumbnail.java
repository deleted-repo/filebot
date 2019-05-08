package net.filebot.ui.rename;

import static net.filebot.ui.ThemeSupport.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class BlankThumbnail implements Icon {

	public static final BlankThumbnail BLANK_POSTER = new BlankThumbnail(48, 48, getBlankBackgroundColor(), getPanelSelectionBorderColor(), 0.68f, 1f);

	private int width;
	private int height;

	private Color fill;
	private Color draw;

	private float squeezeX;
	private float squeezeY;

	public BlankThumbnail(int width, int height, Color fill, Color draw, float squeezeX, float squeezeY) {
		this.width = width;
		this.height = height;
		this.fill = fill;
		this.draw = draw;
		this.squeezeX = squeezeX;
		this.squeezeY = squeezeY;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		int w = (int) (width * squeezeX);
		int h = (int) (height * squeezeY);
		x = (int) (x + (width - w) / 2);
		y = (int) (y + (width - h) / 2);

		g.setColor(fill);
		g.fillRect(x, y, w, h);

		g.setColor(draw);
		g.drawRect(x, y, w, h);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

}