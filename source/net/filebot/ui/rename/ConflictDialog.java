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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.filebot.ResourceManager;
import net.filebot.UserFiles;
import net.miginfocom.swing.MigLayout;

class ConflictDialog extends JDialog {

	private ConflictTableModel model = new ConflictTableModel();
	private boolean cancel = true;

	public ConflictDialog(Window owner, List<Conflict> conflicts) {
		super(owner, "Conflicting Files", ModalityType.DOCUMENT_MODAL);

		model.setData(conflicts);

		JTable table = new JTable(model);
		table.setDefaultRenderer(File.class, new FileRenderer());
		table.setDefaultRenderer(Conflict.class, new ConflictRenderer());
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setAutoCreateColumnsFromModel(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.setRowHeight(25);
		table.setPreferredScrollableViewportSize(new Dimension(500, 250));

		table.addMouseListener(new OpenListener());

		// force white background (e.g. GTK LaF default table background is gray)
		setBackground(getPanelBackground());

		JComponent c = (JComponent) getContentPane();
		c.setLayout(new MigLayout("insets dialog, nogrid, fill", "", "[fill][pref!]"));

		c.add(new JScrollPane(table), "grow, wrap");
		c.add(newButton("Cancel", ResourceManager.getIcon("dialog.cancel"), this::cancel), "tag left");
		c.add(newButton("Continue", ResourceManager.getIcon("dialog.continue"), this::ok), "tag ok");

		JButton b = newButton("Override", ResourceManager.getIcon("dialog.continue.invalid"), this::override);
		b.setEnabled(conflicts.stream().anyMatch(it -> it.override));
		b.addActionListener(evt -> b.setEnabled(false));
		c.add(b, "tag next");

		// focus "Continue" button
		SwingUtilities.invokeLater(c.getComponent(2)::requestFocusInWindow);

		installAction(c, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), newAction("Cancel", this::cancel));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(640, 280));
		pack();
	}

	public boolean cancel() {
		return cancel;
	}

	public List<Conflict> getConflicts() {
		return model.getData();
	}

	private void override(ActionEvent evt) {
		// delete existing destination files and create new data model
		List<Conflict> data = model.getData().stream().map(c -> {
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
		model.setData(data);

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

	public static class Conflict {

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

		@Override
		public String toString() {
			return issues.toString();
		}

		public enum Kind {

			OVERRIDE_FAILED, DUPLICATE_SOURCE, DUPLICATE_DESTINATION, DESTINATION_ALREADY_EXISTS, MISSING_EXTENSION;

			@Override
			public String toString() {
				switch (this) {
				case DUPLICATE_SOURCE:
					return "Duplicate source path";
				case DUPLICATE_DESTINATION:
					return "Duplicate destination path";
				case DESTINATION_ALREADY_EXISTS:
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

	private static class ConflictTableModel extends AbstractTableModel {

		private Conflict[] data = new Conflict[0];

		public void setData(List<Conflict> data) {
			this.data = data.toArray(new Conflict[0]);

			// update table
			fireTableDataChanged();
		}

		private List<Conflict> getData() {
			return unmodifiableList(asList(data));
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "";
			case 1:
				return "Issue";
			case 2:
				return "Source";
			case 3:
				return "Destination";
			}
			return null;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return Icon.class;
			case 1:
				return Conflict.class;
			case 2:
				return File.class;
			case 3:
				return File.class;
			}
			return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Conflict conflict = data[row];

			switch (column) {
			case 0:
				return ResourceManager.getIcon(conflict.issues.isEmpty() ? "status.ok" : "status.error");
			case 1:
				return conflict;
			case 2:
				return conflict.source;
			case 3:
				return conflict.destination;
			}
			return null;
		}
	}

	private static class FileRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			File f = (File) value;
			super.getTableCellRendererComponent(table, f.getName(), isSelected, hasFocus, row, column);
			setToolTipText(f.getPath());
			return this;
		}
	}

	private static class ConflictRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Conflict conflict = (Conflict) value;
			String label = conflict.issues.isEmpty() ? "OK" : conflict.issues.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(joining(" • "));
			super.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
			setToolTipText(formatToolTip(conflict));
			return this;
		}

		private String formatToolTip(Conflict conflict) {
			StringBuilder html = new StringBuilder(64).append("<html>");
			conflict.issues.forEach((k, v) -> appendTooltipParagraph(html, k.toString(), v.toString()));
			return html.append("</html>").toString();
		}

		private StringBuilder appendTooltipParagraph(StringBuilder html, String label, Object value) {
			return html.append("<p style='width:350px; margin:3px'><b>").append(label).append(":</b><br>").append(escapeHTML(value.toString())).append("</p>");
		}
	}

	private static class OpenListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent evt) {
			if (evt.getClickCount() == 2) {
				JTable table = (JTable) evt.getSource();
				int row = table.getSelectedRow();
				if (row >= 0) {
					ConflictTableModel m = (ConflictTableModel) table.getModel();
					Conflict c = m.getData().get(row);

					List<File> files = Stream.of(c.source, c.destination).filter(File::exists).distinct().collect(toList());
					UserFiles.revealFiles(files);
				}
			}
		}
	}

	public static boolean check(Component parent, Map<File, File> renameMap) {
		List<Conflict> conflicts = new ArrayList<Conflict>();

		// sanity checks
		Set<File> destinationFiles = new HashSet<File>();

		renameMap.forEach((from, relativeDestinationPath) -> {
			// resolve relative paths
			File to = resolve(from, relativeDestinationPath);

			Map<Conflict.Kind, String> issues = new EnumMap<Conflict.Kind, String>(Conflict.Kind.class);

			// output files must have a valid file extension
			if (getExtension(to) == null && from.isFile()) {
				String details = String.format("Destination file path [%s] has no file extension. Please adjust your format.", to.getName());
				issues.put(Conflict.Kind.MISSING_EXTENSION, details);
			}

			// one file per unique output path
			if (!destinationFiles.add(to)) {
				List<String> sourceFiles = renameMap.entrySet().stream().filter(e -> e.getValue().equals(relativeDestinationPath)).map(e -> e.getKey().getName()).collect(toList());
				String details = String.format("Multiple source files %s all map to the same destination file [%s]. The highest-quality version will be selected.", sourceFiles, to.getName());
				issues.put(Conflict.Kind.DUPLICATE_DESTINATION, details);
			}

			// check if input and output overlap
			if (renameMap.containsKey(to) && !to.equals(from)) {
				String details = String.format("Overlapping file mapping between [%s ➔ %s] and [%s ➔ %s]. Please adjust your format.", from.getName(), to.getName(), to.getName(), renameMap.get(to).getName());
				issues.put(Conflict.Kind.DUPLICATE_SOURCE, details);
			}

			// check if destination path already exists
			if (to.exists() && !to.equals(from)) {
				String details = String.format("Destination file path [%s] already exists and will be deleted.", to.getName());
				issues.put(Conflict.Kind.DESTINATION_ALREADY_EXISTS, details);
			}

			if (issues.size() > 0) {
				// allow override if this is the only issue
				boolean override = issues.containsKey(Conflict.Kind.DESTINATION_ALREADY_EXISTS) && issues.size() == 1;
				conflicts.add(new Conflict(from, to, issues, override));
			}
		});

		if (conflicts.isEmpty()) {
			return true;
		}

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
