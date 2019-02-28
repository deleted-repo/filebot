package net.filebot.archive;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.filebot.util.ByteBufferOutputStream;
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

		this.command = getCommand(archive);
		this.archive = archive;
	}

	@Override
	public List<FileInfo> listFiles() throws IOException {
		return command.listFiles(archive);
	}

	@Override
	public void extract(File outputFolder) throws IOException {
		command.extract(archive, outputFolder);
	}

	@Override
	public void extract(File outputFolder, FileFilter filter) throws IOException {
		command.extract(archive, outputFolder, filter);
	}

	protected static Command getCommand(File archive) {
		return RAR_FILES.accept(archive) ? Command.unrar : Command.p7zip;
	}

	protected static CharSequence execute(String... command) throws IOException {
		Process process = new ProcessBuilder(command).redirectError(Redirect.INHERIT).start();

		try (ByteBufferOutputStream bb = new ByteBufferOutputStream(8 * 1024)) {
			bb.transferFully(process.getInputStream());

			int returnCode = process.waitFor();
			String output = UTF_8.decode(bb.getByteBuffer()).toString();

			// DEBUG
			debug.fine(format("Execute: %s", asList(command)));
			debug.finest(output);

			if (returnCode == 0) {
				return output;
			} else {
				throw new IOException(String.format("%s failed with exit code %d: %s", asList(command), returnCode, SPACE.matcher(output).replaceAll(" ").trim()));
			}
		} catch (InterruptedException e) {
			throw new IOException(String.format("%s timed out", asList(command)), e);
		}
	}

	public enum Command {

		p7zip {

			@Override
			public String getCommand() {
				return System.getProperty("net.filebot.archive.7z", "7z");
			}

			@Override
			public List<FileInfo> listFiles(File archive) throws IOException {
				CharSequence output = execute(getCommand(), "l", "-slt", "-y", archive.getPath());

				List<FileInfo> paths = new ArrayList<FileInfo>();

				String path = null;
				long size = -1;

				for (String line : NEWLINE.split(output)) {
					int split = line.indexOf(" = ");

					// ignore empty lines
					if (split < 0) {
						continue;
					}

					String key = line.substring(0, split);
					String value = line.substring(split + 3, line.length());

					// ignore empty lines
					if (key.isEmpty() || value.isEmpty()) {
						continue;
					}

					if ("Path".equals(key)) {
						path = value;
					} else if ("Size".equals(key)) {
						size = Long.parseLong(value);
					}

					if (path != null && size >= 0) {
						paths.add(new SimpleFileInfo(path, size));

						path = null;
						size = -1;
					}
				}

				return paths;
			}

			@Override
			public void extract(File archive, File outputFolder) throws IOException {
				execute(getCommand(), "x", "-y", "-aos", archive.getPath(), "-o" + outputFolder.getCanonicalPath());
			}

			@Override
			public void extract(File archive, File outputFolder, FileFilter filter) throws IOException {
				Stream<String> command = Stream.of(getCommand(), "x", "-y", "-aos", archive.getPath(), "-o" + outputFolder.getCanonicalPath());
				Stream<String> selection = listFiles(archive).stream().filter(f -> filter.accept(f.toFile())).map(FileInfo::getPath);

				execute(Stream.concat(command, selection).toArray(String[]::new));
			}

			@Override
			public String version() throws IOException {
				CharSequence output = execute(getCommand());

				return NEWLINE.splitAsStream(output).map(String::trim).filter(s -> s.startsWith("p7zip")).findFirst().get();
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
					return r.length == 5 && r[4].length() > 0 && DIGIT.matcher(r[1]).matches();
				}).map(r -> {
					return new SimpleFileInfo(r[4], Long.parseLong(r[1]));
				}).collect(toList());
			}

			@Override
			public void extract(File archive, File outputFolder) throws IOException {
				execute(getCommand(), "x", "-y", archive.getPath(), outputFolder.getCanonicalPath());
			}

			@Override
			public void extract(File archive, File outputFolder, FileFilter filter) throws IOException {
				Stream<String> command = Stream.of(getCommand(), "x", "-y", archive.getPath());
				Stream<String> selection = listFiles(archive).stream().filter(f -> filter.accept(f.toFile())).map(FileInfo::getPath);
				Stream<String> output = Stream.of(outputFolder.getCanonicalPath());

				execute(Stream.concat(command, Stream.concat(selection, output)).toArray(String[]::new));
			}

			@Override
			public String version() throws IOException {
				CharSequence output = execute(getCommand());

				return Pattern.compile("\\n|\\s\\s+").splitAsStream(output).map(String::trim).filter(s -> s.length() > 0).findFirst().get();
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
