package net.filebot.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

public class RoundDecoration implements Icon {

	private Icon icon;

	private Color fill;
	private Color draw;

	private int width;
	private int height;

	public RoundDecoration(Icon icon, int width, int height, Color fill, Color draw) {
		this.icon = icon;
		this.width = width;
		this.height = height;
		this.fill = fill;
		this.draw = draw;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape shape = new Ellipse2D.Float(x, y, width - 1, height - 1);

		g2d.setColor(fill);
		g2d.fill(shape);

		g2d.setColor(draw);
		g2d.draw(shape);

		icon.paintIcon(c, g, x + (width - icon.getIconWidth()) / 2, y + (height - icon.getIconHeight()) / 2);
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