package net.filebot.ui.filter;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static javax.swing.BorderFactory.*;
import static net.filebot.Logging.*;
import static net.filebot.MediaTypes.*;
import static net.filebot.Settings.*;
import static net.filebot.media.MediaDetection.*;
import static net.filebot.util.FileUtilities.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

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

		// meta type groups
		groupParallel(filesAndFolders).forEach((type, files) -> {
			if (files.size() > 0) {
				groups.add(createStatisticsNode(type.toString(), files));
			}
		});

		// file type groups
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

	protected Map<MetaType, List<File>> groupParallel(List<File> files) {
		Map<MetaType, List<File>> metaTypes = new EnumMap<MetaType, List<File>>(MetaType.class);

		ExecutorService threadPool = Executors.newFixedThreadPool(getPreferredThreadPoolSize());
		try {
			files.stream().collect(toMap(f -> f, f -> threadPool.submit(() -> MetaType.classify(f)), (a, b) -> a, LinkedHashMap::new)).forEach((f, classes) -> {
				if (!classes.isCancelled()) {
					try {
						classes.get().forEach(type -> {
							metaTypes.computeIfAbsent(type, t -> new ArrayList<File>()).add(f);
						});
					} catch (InterruptedException e) {
						throw new CancellationException();
					} catch (Exception e) {
						debug.log(Level.SEVERE, e, e::toString);
					}
				}

				if (Thread.interrupted()) {
					throw new CancellationException();
				}
			});
		} finally {
			threadPool.shutdownNow();
		}

		return metaTypes;
	}

	@Override
	protected void setModel(TreeModel model) {
		tree.setModel(model);
	}

	public static enum MetaType implements FileFilter {

		EPISODE("Episode", f -> VIDEO_FILES.accept(f) && isEpisode(f, true)),

		MOVIE("Movie", f -> VIDEO_FILES.accept(f) && isMovie(f, true)),

		MOVIE_FOLDER("Movie Folder", f -> f.isDirectory() && isMovie(f, true)),

		DISK_FOLDER("Disk Folder", getDiskFolderFilter()),

		VIDEO("Video", VIDEO_FILES),

		SUBTITLE("Subtitle", SUBTITLE_FILES),

		AUDIO("Audio", AUDIO_FILES),

		ARCHIVE("Archive", ARCHIVE_FILES),

		VERIFICATION("Verification", VERIFICATION_FILES),

		EXTRAS("Extras", getClutterFileFilter()),

		CLUTTER("Clutter", getClutterTypeFilter());

		private final String name;
		private final FileFilter filter;

		private MetaType(String name, FileFilter filter) {
			this.name = name;
			this.filter = filter;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean accept(File f) {
			return filter.accept(f);
		}

		public static Set<MetaType> classify(File f) {
			Set<MetaType> classes = EnumSet.noneOf(MetaType.class);
			for (MetaType t : MetaType.values()) {
				if (t.accept(f)) {
					classes.add(t);
				}
			}
			return classes;
		}
	}

}
