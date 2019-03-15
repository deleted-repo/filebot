package net.filebot.platform.bsd;

import static java.util.stream.Collectors.*;
import static net.filebot.Execute.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.filebot.util.XattrView;

public class ExtAttrView implements XattrView {

	private final String path;

	public ExtAttrView(Path path) {
		this.path = path.toString();
	}

	public List<String> list() throws IOException {
		return SPACE.splitAsStream(execute("lsextattr", "-q", "user", path)).map(String::trim).filter(s -> s.length() > 0).collect(toList());
	}

	public String read(String key) {
		try {
			return execute("getextattr", "-q", "user", key, path).toString().trim();
		} catch (IOException e) {
			return null;
		}
	}

	public void write(String key, String value) throws IOException {
		execute("setextattr", "-q", "user", key, value, path);
	}

	public void delete(String key) throws IOException {
		execute("rmextattr", "-q", "user", key, path);
	}

}
