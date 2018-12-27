package net.filebot.util;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class PlainFileXattrView implements XattrView {

	private static final String XATTR_FOLDER = System.getProperty("net.filebot.xattr.store", ".xattr");

	private final Path folder;

	public PlainFileXattrView(Path path) throws IOException {
		folder = path.getParent().resolve(XATTR_FOLDER).resolve(path.getFileName());
	}

	@Override
	public List<String> list() throws IOException {
		if (Files.isDirectory(folder)) {
			return Files.list(folder).map(Path::getFileName).map(Path::toString).collect(toList());
		}
		return emptyList();
	}

	@Override
	public String read(String key) throws IOException {
		try {
			return new String(Files.readAllBytes(folder.resolve(key)), UTF_8);
		} catch (NoSuchFileException e) {
			return null;
		}
	}

	@Override
	public void write(String key, String value) throws IOException {
		Files.createDirectories(folder);
		Files.write(folder.resolve(key), value.getBytes(UTF_8));
	}

	@Override
	public void delete(String key) throws IOException {
		Files.deleteIfExists(folder.resolve(key));
	}

}
