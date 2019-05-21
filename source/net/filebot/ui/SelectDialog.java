package net.filebot.ui;

import static java.awt.Cursor.*;
import static javax.swing.SwingUtilities.*;
import static net.filebot.ui.ThemeSupport.*;
import static net.filebot.util.ui.SwingUI.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicCheckBoxUI;

import net.filebot.ResourceManager;
import net.filebot.util.ui.DefaultFancyListCellRenderer;
import net.filebot.util.ui.RoundDecoration;
import net.filebot.web.SearchResult;
import net.filebot.web.TheTVDBSearchResult;
import net.miginfocom.swing.MigLayout;

public class SelectDialog<T> extends JDialog {

	private JLabel messageLabel = new JLabel();
	private JToggleButton autoRepeatCheckBox;

	private JList<T> list;
	private String command;

	private Function<T, Icon> icon;

	public SelectDialog(Component parent, Collection<? extends T> options) {
		this(parent, options, null, false, false, null);
	}

	public SelectDialog(Component parent, Collection<? extends T> options, Function<T, Icon> icon, boolean autoRepeatEnabled, boolean autoRepeatSelected, JComponent header) {
		super(getWindow(parent), "Select", ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// enable icons
		this.icon = icon;

		// initialize list
		list = new JList(options.toArray());

		// select first element
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);

		DefaultFancyListCellRenderer renderer = new DefaultFancyListCellRenderer(4) {

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, convertValueToString(value), index, isSelected, cellHasFocus);
				configureValue(this, value);
				return this;
			}
		};

		renderer.setHighlightingEnabled(false);

		list.setCellRenderer(renderer);
		list.addMouseListener(mouseListener);

		JComponent c = (JComponent) getContentPane();
		c.setLayout(new MigLayout("insets 1.5mm 1.5mm 2.7mm 1.5mm, nogrid, novisualpadding, fill", "", header == null ? "[pref!][fill][pref!]" : "[min!][min!][fill][pref!]"));

		if (header != null) {
			c.add(header, "wmin 150px, hmin pref, growx, wrap");
		}
		c.add(messageLabel, "wmin 150px, hmin pref, growx, wrap");
		c.add(new JScrollPane(list), "wmin 150px, hmin 150px, grow, wrap 2mm");

		c.add(new JButton(selectAction), "align center, id select");
		c.add(new JButton(cancelAction), "gap unrel, id cancel");

		// add repeat button
		if (autoRepeatEnabled) {
			autoRepeatCheckBox = createAutoRepeatCheckBox();
			c.add(autoRepeatCheckBox, "pos 1al select.y-3 n select.y2");
		}

		// set default size and location
		setMinimumSize(new Dimension(400, 330));

		// Shortcut Enter
		installAction(list, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), selectAction);
	}

	protected JToggleButton createAutoRepeatCheckBox() {
		JCheckBox c = new JCheckBox();
		c.setUI(new BasicCheckBoxUI());
		c.setCursor(getPredefinedCursor(HAND_CURSOR));
		c.setIcon(createAutoRepeatCheckBoxIcon("button.repeat"));
		c.setSelectedIcon(createAutoRepeatCheckBoxIcon("button.repeat.selected"));
		c.addChangeListener(evt -> autoRepeatCheckBox.setToolTipText(autoRepeatCheckBox.isSelected() ? "Select and remember for next time" : "Select once and ask again next time"));
		return c;
	}

	protected Icon createAutoRepeatCheckBoxIcon(String name) {
		return new RoundDecoration(ResourceManager.getIcon(name), 28, 28, getPanelBackground(), getColor(0xD7D7D7));
	}

	protected String convertValueToString(Object value) {
		return value.toString();
	}

	protected void configureValue(DefaultFancyListCellRenderer render, Object value) {
		if (value instanceof SearchResult) {
			render.setToolTipText(getTooltipText((SearchResult) value));
		} else if (value instanceof File) {
			render.setToolTipText(((File) value).getAbsolutePath());
		} else {
			render.setToolTipText(null);
		}

		if (icon != null) {
			render.setIcon(icon.apply((T) value));
		}
	}

	protected String getTooltipText(SearchResult item) {
		StringBuilder html = new StringBuilder(64);
		html.append("<html><h3>").append(escapeHTML(item.toString())).append("</h3>");

		String[] names = item.getAliasNames();
		if (names.length > 0) {
			appendTooltipParagraph(html, "AKA", String.join(" | ", names));
		}

		if (item instanceof TheTVDBSearchResult) {
			TheTVDBSearchResult r = (TheTVDBSearchResult) item;

			appendTooltipParagraph(html, "First Aired", r.getFirstAired());
			appendTooltipParagraph(html, "Network", r.getNetwork());
			appendTooltipParagraph(html, "Status", r.getStatus());
			appendTooltipParagraph(html, "Overview", r.getOverview());
		}

		appendTooltipParagraph(html, "ID", item.getId());

		return html.append("</html>").toString();
	}

	private StringBuilder appendTooltipParagraph(StringBuilder html, String label, Object value) {
		if (value != null) {
			html.append("<p style='width:250px; margin:3px'><b>").append(label).append(":</b> ").append(escapeHTML(value.toString())).append("</p>");
		}
		return html;
	}

	public JLabel getMessageLabel() {
		return messageLabel;
	}

	public JToggleButton getAutoRepeatCheckBox() {
		return autoRepeatCheckBox;
	}

	public String getSelectedAction() {
		return command;
	}

	public T getSelectedValue() {
		return SELECT.equals(command) ? list.getSelectedValue() : null;
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public Action getSelectAction() {
		return selectAction;
	}

	public Action getCancelAction() {
		return cancelAction;
	}

	public static final String SELECT = "Select";
	public static final String CANCEL = "Cancel";

	private final Action selectAction = newAction(SELECT, ResourceManager.getIcon("dialog.continue"), evt -> {
		command = SELECT;
		close();
	});

	private final Action cancelAction = newAction(CANCEL, ResourceManager.getIcon("dialog.cancel"), evt -> {
		command = CANCEL;
		close();
	});

	private final MouseAdapter mouseListener = mouseClicked(evt -> {
		if (isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
			selectAction.actionPerformed(new ActionEvent(evt.getSource(), ActionEvent.ACTION_PERFORMED, SELECT));
		}
	});

	private static final String KEY_REPEAT = "dialog.select.repeat";
	private static final String KEY_WIDTH = "dialog.select.width";
	private static final String KEY_HEIGHT = "dialog.select.height";

	public void saveState(Preferences prefs) {
		prefs.putInt(KEY_WIDTH, getWidth());
		prefs.putInt(KEY_HEIGHT, getHeight());

		if (autoRepeatCheckBox != null) {
			prefs.putBoolean(KEY_REPEAT, autoRepeatCheckBox.isSelected());
		}
	}

	public void restoreState(Preferences prefs) {
		setSize(prefs.getInt(KEY_WIDTH, getWidth()), prefs.getInt(KEY_HEIGHT, getHeight()));

		if (autoRepeatCheckBox != null) {
			autoRepeatCheckBox.setSelected(prefs.getBoolean(KEY_REPEAT, autoRepeatCheckBox.isSelected()));
		}
	}

}
