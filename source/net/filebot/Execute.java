package net.filebot;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import net.filebot.util.ByteBufferOutputStream;

public class Execute {

	private static final int BUFFER_SIZE = 8 * 1024;

	public static CharSequence execute(String... command) throws IOException {
		return execute(asList(command), null);
	}

	public static CharSequence execute(String[] command, File directory) throws IOException {
		return execute(asList(command), directory);
	}

	public static CharSequence execute(String[]... command) throws IOException {
		return execute(stream(command).flatMap(a -> stream(a)).collect(toList()), null);
	}

	public static CharSequence execute(List<String> command, File directory) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectError(Redirect.INHERIT);

		if (directory != null) {
			processBuilder.directory(directory);
		}

		Process process = processBuilder.start();

		try (ByteBufferOutputStream bb = new ByteBufferOutputStream(BUFFER_SIZE)) {
			bb.transferFully(process.getInputStream());

			int exitCode = process.waitFor();
			CharSequence output = UTF_8.decode(bb.getByteBuffer());

			// DEBUG
			debug.finest(format("Execute %s%n%s", command, output));

			if (exitCode != 0) {
				throw new ExecuteException(command, exitCode);
			}

			return output;
		} catch (InterruptedException e) {
			throw new IOException(String.format("%s timed out", command), e);
		}
	}

	public static void system(String... command) throws IOException {
		system(asList(command), null);
	}

	public static void system(String[] command, File directory) throws IOException {
		system(asList(command), directory);
	}

	public static void system(List<String> command, File directory) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.inheritIO();

		if (directory != null) {
			processBuilder.directory(directory);
		}

		// DEBUG
		debug.finest(format("Execute %s", command));

		try {
			int exitCode = processBuilder.start().waitFor();
			if (exitCode != 0) {
				throw new ExecuteException(command, exitCode);
			}
		} catch (InterruptedException e) {
			throw new IOException(String.format("%s timed out", command), e);
		}
	}

}
