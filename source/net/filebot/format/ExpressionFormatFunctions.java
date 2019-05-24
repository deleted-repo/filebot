package net.filebot.format;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Settings.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.sun.jna.Platform;

import groovy.lang.Closure;
import groovy.lang.Script;
import groovy.util.XmlSlurper;
import net.filebot.ApplicationFolder;
import net.filebot.platform.mac.MacAppUtilities;
import net.filebot.util.FileUtilities;

/**
 * Global functions available in the {@link ExpressionFormat}
 */
public class ExpressionFormatFunctions {

	/*
	 * General helpers and utilities
	 */

	public static Object call(Script context, Object object) {
		if (object instanceof Closure) {
			try {
				return call(context, ((Closure) object).call());
			} catch (Exception e) {
				return null;
			}
		}

		if (isEmptyValue(context, object)) {
			return null;
		}

		return object;
	}

	public static boolean isEmptyValue(Script context, Object object) {
		// treat empty string as null
		if (object instanceof CharSequence && object.toString().isEmpty()) {
			return true;
		}

		// treat empty list as null
		if (object instanceof Collection && ((Collection) object).isEmpty()) {
			return true;
		}

		return false;
	}

	public static Object any(Script context, Object c1, Object c2, Object... cN) {
		return stream(context, c1, c2, cN).findFirst().orElse(null);
	}

	public static List<Object> allOf(Script context, Object c1, Object c2, Object... cN) {
		return stream(context, c1, c2, cN).collect(toList());
	}

	public static String concat(Script context, Object c1, Object c2, Object... cN) {
		return stream(context, c1, c2, cN).map(Objects::toString).collect(joining());
	}

	private static Stream<Object> stream(Script context, Object c1, Object c2, Object... cN) {
		return Stream.concat(Stream.of(c1, c2), Stream.of(cN)).map(c -> call(context, c)).filter(Objects::nonNull);
	}

	/*
	 * Unix Shell / Windows PowerShell utilities
	 */

	public static String quote(Script context, Object c1, Object... cN) {
		return Platform.isWindows() ? quotePowerShell(context, c1, cN) : quoteBash(context, c1, cN);
	}

	public static String quoteBash(Script context, Object c1, Object... cN) {
		return stream(context, c1, null, cN).map(Objects::toString).map(s -> "'" + s.replace("'", "'\"'\"'") + "'").collect(joining(" "));
	}

	public static String quotePowerShell(Script context, Object c1, Object... cN) {
		return stream(context, c1, null, cN).map(Objects::toString).map(s -> "@'\n" + s + "\n'@").collect(joining(" "));
	}

	/*
	 * I/O utilities
	 */

	public static Map<String, String> csv(Script context, Object path) throws IOException {
		Pattern[] delimiter = { TAB, SEMICOLON };
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (String line : readLines(context, path)) {
			for (Pattern d : delimiter) {
				String[] field = d.split(line, 2);
				if (field.length >= 2) {
					map.put(field[0].trim(), field[1].trim());
					break;
				}
			}
		}
		return map;
	}

	public static List<String> readLines(Script context, Object path) throws IOException {
		return FileUtilities.readLines(getUserFile(context, path));
	}

	public static Object readXml(Script context, Object path) throws Exception {
		return new XmlSlurper().parse(getUserFile(context, path));
	}

	public static File getUserFile(Script context, Object path) {
		File f = new File(path.toString());

		if (!f.isAbsolute()) {
			f = ApplicationFolder.UserHome.resolve(f.getPath());
		}

		if (isMacSandbox()) {
			MacAppUtilities.askUnlockFolders(null, singleton(f));
		}

		return f;

	}

	public static Object include(Script context, Object path) throws Exception {
		return context.evaluate(getUserFile(context, path));
	}

	private ExpressionFormatFunctions() {
		throw new UnsupportedOperationException();
	}

}
