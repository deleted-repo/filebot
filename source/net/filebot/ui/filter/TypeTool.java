package net.filebot.ui.filter;

import static java.util.Collections.*;
import static javax.swing.BorderFactory.*;
import static net.filebot.MediaTypes.*;
import static net.filebot.media.MediaDetection.*;
import static net.filebot.util.FileUtilities.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.filebot.ui.filter.FileTree.FolderNode;
import net.filebot.ui.transfer.DefaultTransferHandler;
import net.filebot.util.ui.LoadingOverlayPane;
import net.miginfocom.swing.MigLayout;

class TypeTool extends Tool<TreeModel> {

	private FileTree tree = new FileTree();

	public TypeTool() {
		super("Types");

		setLayout(new MigLayout("insets 0, fill"));
		JScrollPane treeScrollPane = new JScrollPane(tree);
		treeScrollPane.setBorder(createEmptyBorder());

		add(new LoadingOverlayPane(treeScrollPane, this), "grow");

		tree.setTransferHandler(new DefaultTransferHandler(null, new FileTreeExportHandler()));
		tree.setDragEnabled(true);
	}

	@Override
	protected TreeModel createModelInBackground(List<File> root) {
		if (root.isEmpty()) {
			return new DefaultTreeModel(new FolderNode("Types", emptyList()));
		}

		List<File> filesAndFolders = listFiles(root, NOT_HIDDEN, HUMAN_NAME_ORDER);

		List<TreeNode> groups = new ArrayList<TreeNode>();

		for (Entry<String, FileFilter> it : getMetaTypes().entrySet()) {
			List<File> selection = filter(filesAndFolders, it.getValue());
			if (selection.size() > 0) {
				groups.add(createStatisticsNode(it.getKey(), selection));
			}

			if (Thread.interrupted()) {
				throw new CancellationException();
			}
		}

		SortedMap<String, TreeNode> extensionGroups = new TreeMap<String, TreeNode>(String.CASE_INSENSITIVE_ORDER);

		for (Entry<String, List<File>> it : mapByExtension(filter(filesAndFolders, FILES)).entrySet()) {
			if (it.getKey() != null) {
				extensionGroups.put(it.getKey(), createStatisticsNode(it.getKey(), it.getValue()));
			}

			if (Thread.interrupted()) {
				throw new CancellationException();
			}
		}

		groups.addAll(extensionGroups.values());

		// create tree model
		return new DefaultTreeModel(new FolderNode("Types", groups));
	}

	public Map<String, FileFilter> getMetaTypes() {
		Map<String, FileFilter> types = new LinkedHashMap<String, FileFilter>();
		types.put("Movie", f -> VIDEO_FILES.accept(f) && isMovie(f, true));
		types.put("Episode", f -> VIDEO_FILES.accept(f) && isEpisode(f, true));
		types.put("Disk Folder", getDiskFolderFilter());
		types.put("Video", VIDEO_FILES);
		types.put("Subtitle", SUBTITLE_FILES);
		types.put("Audio", AUDIO_FILES);
		types.put("Archive", ARCHIVE_FILES);
		types.put("Verification", VERIFICATION_FILES);
		types.put("Extras", getClutterFileFilter());
		types.put("Clutter", getClutterTypeFilter());
		return types;
	}

	@Override
	protected void setModel(TreeModel model) {
		tree.setModel(model);
	}

}
