package net.filebot.platform.bsd;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Logging.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.List;

import net.filebot.util.ByteBufferOutputStream;

public class ExtAttrView {

	private final String path;

	public ExtAttrView(Path path) {
		this.path = path.toAbsolutePath().toString();
	}

	public List<String> list() throws IOException {
		CharSequence output = execute("lsextattr", "-q", "user", path);

		return SPACE.splitAsStream(output).map(String::trim).filter(s -> s.length() > 0).collect(toList());
	}

	public String read(String key) throws IOException {
		CharSequence output = execute("getextattr", "-q", "user", key, path);

		return output.toString().trim();
	}

	public void write(String key, String value) throws IOException {
		execute("setextattr", "-q", "user", key, value, path);
	}

	public void delete(String key) throws IOException {
		execute("rmextattr", "-q", "user", key, path);
	}

	protected CharSequence execute(String... command) throws IOException {
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
				throw new IOException(String.format("%s failed with exit code %d: %s", command[0], returnCode, SPACE.matcher(output).replaceAll(" ").trim()));
			}
		} catch (InterruptedException e) {
			throw new IOException(String.format("%s timed out", command[0]), e);
		}
	}

}
