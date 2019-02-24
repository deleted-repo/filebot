package net.filebot.ui;

import static javax.swing.BorderFactory.*;
import static net.filebot.ui.ThemeSupport.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.filebot.util.ui.notification.SeparatorBorder.Position;

public class HeaderPanel extends JComponent {

	private JLabel titleLabel = new JLabel();

	public HeaderPanel() {
		setLayout(new BorderLayout());
		setBackground(getPanelBackground());

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setOpaque(false);

		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setVerticalAlignment(SwingConstants.CENTER);
		titleLabel.setOpaque(false);
		titleLabel.setForeground(getLabelForeground());
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));

		centerPanel.setBorder(createEmptyBorder());
		centerPanel.add(titleLabel, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		setBorder(getSeparatorBorder(Position.BOTTOM));
	}

	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(getPanelBackgroundGradient(0, 0, getWidth(), 0));
		g2d.fill(getBounds());
	}

}
