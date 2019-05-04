package net.filebot.ui.rename;

import static net.filebot.ui.ThemeSupport.*;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.Icon;

public class BlankThumbnail implements Icon {

	public static final BlankThumbnail BLANK_POSTER = new BlankThumbnail(48, 48, getColor(0xF8F8FF), getPanelSelectionBorderColor(), 0.68f, 1f);

	private int width;
	private int height;

	private Paint fill;
	private Paint draw;

	private float squeezeX;
	private float squeezeY;

	public BlankThumbnail(int width, int height, Paint fill, Paint draw, float squeezeX, float squeezeY) {
		this.width = width;
		this.height = height;
		this.fill = fill;
		this.draw = draw;
		this.squeezeX = squeezeX;
		this.squeezeY = squeezeY;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g;

		int w = (int) (width * squeezeX);
		int h = (int) (height * squeezeY);
		x = (int) (x + (width - w) / 2);
		y = (int) (y + (width - h) / 2);

		g2d.setPaint(fill);
		g2d.fillRect(x, y, w, h);

		g2d.setPaint(draw);
		g2d.drawRect(x, y, w, h);
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