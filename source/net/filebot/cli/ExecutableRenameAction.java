package net.filebot.cli;

import static net.filebot.Execute.*;

import java.io.File;

import net.filebot.RenameAction;

public class ExecutableRenameAction implements RenameAction {

	private final String executable;
	private final File directory;

	public ExecutableRenameAction(String executable, File directory) {
		this.executable = executable;
		this.directory = directory;
	}

	@Override
	public File rename(File from, File to) throws Exception {
		String[] command = { executable, from.getCanonicalPath(), getRelativePath(directory, to) };

		system(command, directory);

		return to.exists() ? to : null;
	}

	private String getRelativePath(File dir, File f) {
		return dir == null ? f.toString() : dir.toPath().relativize(f.toPath()).toString();
	}

	@Override
	public boolean canRevert() {
		return false;
	}

	@Override
	public String toString() {
		return executable;
	}

}
