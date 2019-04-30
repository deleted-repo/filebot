package net.filebot.util.ui;

import static javax.swing.SwingUtilities.*;
import static net.filebot.Logging.*;
import static net.filebot.ui.ThemeSupport.*;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

public class ActionPopup extends JPopupMenu {

	protected final JLabel headerLabel = new JLabel();
	protected final JLabel descriptionLabel = new JLabel();
	protected final JLabel statusLabel = new JLabel();

	protected final JPanel actionPanel = new JPanel(new MigLayout("nogrid, insets 0, fill"));

	public ActionPopup(String label, Icon icon) {
		// fix text color (especially on Linux with dark GTK theme)
		setForeground(new JMenuItem().getForeground());

		headerLabel.setText(label);
		headerLabel.setForeground(getForeground());
		headerLabel.setIcon(icon);
		headerLabel.setIconTextGap(5);

		actionPanel.setOpaque(false);

		statusLabel.setFont(statusLabel.getFont().deriveFont(10f));
		statusLabel.setForeground(getPassiveColor());

		setLayout(new MigLayout("nogrid, fill, insets 0"));

		add(headerLabel, "gapx 5px 5px, gapy 3px 1px, wrap 3px");
		add(new JSeparator(), "growx, wrap 1px");
		add(actionPanel, "growx, wrap 0px");
		add(new JSeparator(), "growx, wrap 0px");
		add(statusLabel, "growx, h 11px!, gapx 3px, wrap 1px");

		// make it look better (e.g. window shadows) by forcing heavy-weight windows
		setLightWeightPopupEnabled(false);
	}

	@Override
	public void show(Component invoker, int x, int y) {
		super.show(invoker, x, y);

		// make sure that the popup window displays above the task bar
		try {
			getWindowAncestor(this).setAlwaysOnTop(true);
		} catch (Exception e) {
			debug.log(Level.WARNING, e, e::getMessage);
		}
	}

	protected JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(getForeground());
		return label;
	}

	public void addDescription(String text) {
		actionPanel.add(createLabel(text), "gapx 4px 4px, growx, wrap 3px");
	}

	@Override
	public void addSeparator() {
		actionPanel.add(new JSeparator(), "growx, wrap 1px");
	}

	@Override
	public JMenuItem add(Action a) {
		LinkButton link = new LinkButton(a);

		// underline text
		link.setText(String.format("<html><nobr><u>%s</u></nobr></html>", link.getText()));

		// use rollover color
		link.setRolloverEnabled(false);
		link.setColor(link.getRolloverColor());

		// close popup when action is triggered
		link.addActionListener(closeListener);

		actionPanel.add(link, "gapx 12px 12px, growx, wrap");
		return null;
	}

	@Override
	public String getLabel() {
		return headerLabel.getText();
	}

	public void setStatus(String string) {
		statusLabel.setText(string);
	}

	private final ActionListener closeListener = evt -> setVisible(false);

}
