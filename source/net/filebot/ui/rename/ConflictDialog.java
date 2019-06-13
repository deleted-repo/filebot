package net.filebot.ui.rename;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.ui.ThemeSupport.*;
import static net.filebot.util.FileUtilities.*;
import static net.filebot.util.ui.SwingUI.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import net.filebot.ResourceManager;
import net.filebot.UserFiles;
import net.filebot.media.VideoQuality;
import net.filebot.util.ui.DashedSeparator;
import net.filebot.util.ui.DefaultFancyListCellRenderer;
import net.miginfocom.swing.MigLayout;

class ConflictDialog extends JDialog {

	private EventList<Conflict> model = new BasicEventList<Conflict>();
	private boolean cancel = true;

	public ConflictDialog(Window owner, List<Conflict> conflicts) {
		super(owner, "Conflicts", ModalityType.DOCUMENT_MODAL);
		model.addAll(conflicts);

		JList<Conflict> list = new JList<Conflict>(new DefaultEventListModel<Conflict>(model));
		list.setCellRenderer(new ConflictCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// force white background (e.g. GTK LaF default table background is gray)
		setBackground(getPanelBackground());

		JComponent c = (JComponent) getContentPane();
		c.setLayout(new MigLayout("insets dialog, nogrid, fill", "", "[fill][pref!]"));

		c.add(new JScrollPane(list), "grow, wrap");
		c.add(newButton("Cancel", ResourceManager.getIcon("dialog.cancel"), this::cancel), "tag left");
		c.add(newButton("Continue", ResourceManager.getIcon("dialog.continue"), this::ok), "tag ok");

		JButton overrideButton = newButton("Override", ResourceManager.getIcon("dialog.continue.invalid"), this::override);
		overrideButton.setEnabled(conflicts.stream().anyMatch(it -> it.override));
		overrideButton.addActionListener(evt -> overrideButton.setEnabled(false));
		c.add(overrideButton, "tag next");

		installAction(c, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), newAction("Cancel", this::cancel));

		list.addMouseListener(mouseClicked(evt -> {
			if (evt.getClickCount() == 2) {
				Conflict selection = list.getSelectedValue();
				if (selection != null) {
					List<File> files = Stream.of(selection.source, selection.destination).filter(File::exists).distinct().collect(toList());
					UserFiles.revealFiles(files);
				}
			}
		}));

		// give default focus to "Continue" button
		SwingUtilities.invokeLater(c.getComponent(2)::requestFocusInWindow);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(340, 280));
		pack();
	}

	public boolean cancel() {
		return cancel;
	}

	public List<Conflict> getConflicts() {
		return model;
	}

	private void override(ActionEvent evt) {
		// delete existing destination files and create new data model
		List<Conflict> data = model.stream().map(c -> {
			// safety check
			if (!c.override) {
				return c;
			}

			try {
				UserFiles.trash(c.destination);
			} catch (Exception e) {
				return new Conflict(c.source, c.destination, singletonMap(Conflict.Kind.OVERRIDE_FAILED, e.toString()), false);
			}

			// resolved => remove conflict
			return null;
		}).filter(Objects::nonNull).collect(toList());

		// insert new conflict data
		model.clear();
		model.addAll(data);

		// continue if there are no more conflicts
		if (data.isEmpty()) {
			ok(evt);
		}
	}

	public void ok(ActionEvent evt) {
		cancel = false;
		setVisible(false);
	}

	public void cancel(ActionEvent evt) {
		cancel = true;
		setVisible(false);
	}

	private static class ConflictCellRenderer extends DefaultFancyListCellRenderer {

		public ConflictCellRenderer() {
			setHighlightingEnabled(false);
			setBorder(new CompoundBorder(new DashedSeparator(2, 4, getColor(0xEEEEEE), getPanelBackground()), getBorder()));
		}

		@Override
		protected void configureListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Conflict conflict = (Conflict) value;
			super.configureListCellRendererComponent(list, conflict.getDetails(), index, isSelected, cellHasFocus);
			setIcon(ResourceManager.getIcon("status.warning"));
			setToolTipText(formatToolTip(conflict));

			// don't paint border on last element
			setBorderPainted(index < list.getModel().getSize() - 1);
		}

		private String formatToolTip(Conflict conflict) {
			StringBuilder html = new StringBuilder(64).append("<html>");
			appendTooltipParagraph(html, "Conflicts", conflict.issues.keySet().stream().map(Objects::toString).collect(joining(" | ")));
			appendTooltipParagraph(html, "Source", conflict.source.getPath());
			appendTooltipParagraph(html, "Destination", conflict.destination.getPath());
			return html.append("</html>").toString();
		}

		private StringBuilder appendTooltipParagraph(StringBuilder html, String label, Object value) {
			return html.append("<p style='width:350px; margin:3px'><b>").append(label).append(":</b><br>").append(escapeHTML(value.toString())).append("</p>");
		}
	}

	public static class Conflict {

		public static final Comparator<Conflict> SEVERITY_ORDER = Comparator.comparingInt(Conflict::getSeverity).thenComparing(Conflict::getDetails);

		public final File source;
		public final File destination;

		public final Map<Kind, String> issues;
		public final boolean override;

		public Conflict(File source, File destination, Map<Kind, String> issues, boolean override) {
			this.source = source;
			this.destination = destination;
			this.issues = issues;
			this.override = override;
		}

		public String getDetails() {
			return issues.values().iterator().next();
		}

		public int getSeverity() {
			return -issues.keySet().iterator().next().ordinal();
		}

		@Override
		public String toString() {
			return issues.toString();
		}

		public enum Kind {

			MISSING_EXTENSION, OVERLAP, DUPLICATE, FILE_EXISTS, OVERRIDE_FAILED;

			@Override
			public String toString() {
				switch (this) {
				case OVERLAP:
					return "Duplicate source path";
				case DUPLICATE:
					return "Duplicate destination path";
				case FILE_EXISTS:
					return "Destination file already exists";
				case MISSING_EXTENSION:
					return "Missing file extension";
				case OVERRIDE_FAILED:
					return "Failed to override destination file";
				}
				return null;
			}
		}
	}

	private static Map<File, List<File>> getSourceFilesForDestination(Map<File, File> renameMap, Comparator<File> order) {
		Map<File, List<File>> duplicates = renameMap.entrySet().stream().collect(groupingBy(e -> {
			return resolve(e.getKey(), e.getValue());
		}, mapping(e -> e.getKey(), toCollection(ArrayList::new))));

		// sort duplicates by video quality
		duplicates.forEach((k, v) -> v.sort(order));

		return duplicates;
	}

	private static String formatDetails(String line1, String line2, Object... args) {
		StringBuilder html = new StringBuilder(64).append("<html><p style='padding:3px 6px'>");
		html.append(String.format(String.join("<br>", line1, line2), stream(args).map(a -> {
			if (a instanceof File) {
				File file = (File) a;
				return file.getName();
			}
			if (a instanceof File[]) {
				File[] files = (File[]) a;
				return stream(files).map(File::getName).collect(joining(" | ", "[", "]"));
			}
			return String.valueOf(a);
		}).map(s -> formatDetailsVariable(s)).toArray()));
		return html.append("</p></html>").toString();
	}

	private static String formatDetailsVariable(String value) {
		return "<nobr><span style='color:#32D515'>" + value + "</span></nobr>";
	}

	public static boolean check(Component parent, Map<File, File> renameMap) {
		List<Conflict> conflicts = new ArrayList<Conflict>();

		Map<File, List<File>> sourceFilesForDestination = getSourceFilesForDestination(renameMap, VideoQuality.DESCENDING_ORDER);

		// sanity checks
		renameMap.forEach((from, v) -> {
			// resolve relative paths
			File to = resolve(from, v);

			Map<Conflict.Kind, String> issues = new EnumMap<Conflict.Kind, String>(Conflict.Kind.class);

			// output files must have a valid file extension
			if (getExtension(to) == null && from.isFile()) {
				String details = formatDetails("Destination file path [%s] has no file extension.", "Fix via Edit Format.", to);
				issues.put(Conflict.Kind.MISSING_EXTENSION, details);
			}

			// one file per unique output path
			File[] duplicates = sourceFilesForDestination.get(to).toArray(new File[0]);
			File chosen = duplicates[0];
			if (duplicates.length > 1 && !from.equals(chosen)) {
				String details = formatDetails("Multiple source files map to the same destination file %s ➔ %s.", "The highest-quality file %s was chosen instead of %s.", duplicates, to, chosen, from);
				issues.put(Conflict.Kind.DUPLICATE, details);
			}

			// check if input and output overlap
			if (renameMap.containsKey(to) && !to.equals(from)) {
				String details = formatDetails("Overlapping file mapping between %s ➔ %s and %s ➔ %s.", "Fix via Edit Format.", from, to, to, renameMap.get(to));
				issues.put(Conflict.Kind.OVERLAP, details);
			}

			// check if destination path already exists
			if (to.exists() && !to.equals(from)) {
				String details = formatDetails("Destination file path %s already exists.", "Fix via Override.", to.getName());
				issues.put(Conflict.Kind.FILE_EXISTS, details);
			}

			if (issues.size() > 0) {
				// allow override if this is the only issue
				boolean override = issues.containsKey(Conflict.Kind.FILE_EXISTS) && issues.size() == 1;
				conflicts.add(new Conflict(from, to, issues, override));
			}
		});

		if (conflicts.isEmpty()) {
			return true;
		}

		conflicts.sort(Conflict.SEVERITY_ORDER);

		ConflictDialog dialog = new ConflictDialog(getWindow(parent), conflicts);
		dialog.setLocation(getOffsetLocation(dialog.getOwner()));
		dialog.setVisible(true);

		if (dialog.cancel()) {
			return false;
		}

		// exclude conflicts from rename map
		for (Conflict it : dialog.getConflicts()) {
			renameMap.remove(it.source);
		}
		return true;
	}

}
