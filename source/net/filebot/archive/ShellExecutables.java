package net.filebot.archive;

import static java.util.stream.Collectors.*;
import static net.filebot.Execute.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import net.filebot.util.FileUtilities.ExtensionFileFilter;
import net.filebot.vfs.FileInfo;
import net.filebot.vfs.SimpleFileInfo;

public class ShellExecutables implements ArchiveExtractor {

	private Command command;
	private File archive;

	public ShellExecutables(File archive) throws Exception {
		if (!archive.exists()) {
			throw new FileNotFoundException(archive.getPath());
		}

		this.archive = archive.getCanonicalFile();
		this.command = getCommand(this.archive);
	}

	@Override
	public List<FileInfo> listFiles() throws IOException {
		return command.listFiles(archive);
	}

	@Override
	public void extract(File outputFolder) throws IOException {
		command.extract(archive, outputFolder.getCanonicalFile());
	}

	@Override
	public void extract(File outputFolder, FileFilter filter) throws IOException {
		command.extract(archive, outputFolder.getCanonicalFile(), filter);
	}

	protected Command getCommand(File archive) {
		return RAR_FILES.accept(archive) ? Command.unrar : Command.p7zip;
	}

	public enum Command {

		p7zip {

			@Override
			public String getCommand() {
				return System.getProperty("net.filebot.archive.7z", "7z");
			}

			@Override
			public List<FileInfo> listFiles(File archive) throws IOException {
				CharSequence output = execute(getCommand(), "l", "-y", archive.getPath());

				return NEWLINE.splitAsStream(output).map(String::trim).map(l -> SPACE.split(l, 6)).filter(r -> {
					return r.length == 6 && !r[5].isEmpty() && NON_DIGIT.matcher(r[2]).matches() && DIGIT.matcher(r[3]).matches();
				}).map(r -> {
					return new SimpleFileInfo(r[5], Long.parseLong(r[3]));
				}).collect(toList());
			}

			@Override
			public void extract(File archive, File outputFolder) throws IOException {
				String[] command = { getCommand(), "x", "-y", "-o" + outputFolder.getPath(), archive.getPath() };

				execute(command);
			}

			@Override
			public void extract(File archive, File outputFolder, FileFilter filter) throws IOException {
				String[] command = { getCommand(), "x", "-y", "-o" + outputFolder.getPath(), archive.getPath() };
				String[] selection = listFiles(archive).stream().filter(f -> filter.accept(f.toFile())).map(FileInfo::getPath).toArray(String[]::new);

				execute(command, selection);
			}

			@Override
			public String version() throws IOException {
				CharSequence output = execute(getCommand());

				return NEWLINE.splitAsStream(output).map(String::trim).filter(s -> s.startsWith("p7zip")).findFirst().orElse(null);
			}
		},

		unrar {

			@Override
			public String getCommand() {
				return System.getProperty("net.filebot.archive.unrar", "unrar");
			}

			@Override
			public List<FileInfo> listFiles(File archive) throws IOException {
				CharSequence output = execute(getCommand(), "l", "-y", archive.getPath());

				return NEWLINE.splitAsStream(output).map(String::trim).map(l -> SPACE.split(l, 5)).filter(r -> {
					return r.length == 5 && r[4].length() > 0 && NON_DIGIT.matcher(r[0]).matches() && DIGIT.matcher(r[1]).matches();
				}).map(r -> {
					return new SimpleFileInfo(r[4], Long.parseLong(r[1]));
				}).collect(toList());
			}

			@Override
			public void extract(File archive, File outputFolder) throws IOException {
				String[] command = { getCommand(), "x", "-y", archive.getPath(), outputFolder.getPath() };

				execute(command);
			}

			@Override
			public void extract(File archive, File outputFolder, FileFilter filter) throws IOException {
				String[] command = { getCommand(), "x", "-y", archive.getPath() };
				String[] selection = listFiles(archive).stream().filter(f -> filter.accept(f.toFile())).map(FileInfo::getPath).toArray(String[]::new);
				String[] output = { outputFolder.getPath() };

				execute(command, selection, output);
			}

			@Override
			public String version() throws IOException {
				CharSequence output = execute(getCommand());

				return Pattern.compile("\\n+|\\s\\s+").splitAsStream(output).map(String::trim).filter(s -> !s.isEmpty()).findFirst().orElse(null);
			}
		};

		public abstract String getCommand();

		public abstract List<FileInfo> listFiles(File archive) throws IOException;

		public abstract void extract(File archive, File outputFolder) throws IOException;

		public abstract void extract(File archive, File outputFolder, FileFilter filter) throws IOException;

		public abstract String version() throws IOException;

	}

	public static final FileFilter RAR_FILES = new ExtensionFileFilter("rar", "r00");

}
