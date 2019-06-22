package net.filebot.format;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Settings.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.File;
import java.io.FileNotFoundException;
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

	public static Map<String, String> csv(Script context, Object path) throws Exception {
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

	public static List<String> readLines(Script context, Object path) throws Exception {
		return FileUtilities.readLines(resolve(context, path));
	}

	public static Object readXml(Script context, Object path) throws Exception {
		return new XmlSlurper().parse(resolve(context, path));
	}

	public static File getUserFile(Script context, Object path) throws Exception {
		File f = path instanceof File ? (File) path : new File(path.toString());

		if (!f.isAbsolute()) {
			f = ApplicationFolder.UserHome.resolve(f.getPath());
		}

		if (isMacSandbox()) {
			MacAppUtilities.askUnlockFolders(null, singleton(f));
		}

		if (!f.exists()) {
			throw new FileNotFoundException("File not found: " + f);
		}

		return f;
	}

	public static Object include(Script context, Object path) throws Exception {
		return context.evaluate(resolve(context, path));
	}

	public static File resolve(Script context, Object path) throws Exception {
		File include = path instanceof File ? (File) path : new File(path.toString());

		// resolve relative path relative to current script file
		if (!include.isAbsolute()) {
			String script = context.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			if (!GROOVY_SCRIPT_CODE_BASE.equals(script)) {
				return getUserFile(context, new File(new File(script).getParentFile(), include.getPath()));
			}
		}

		return getUserFile(context, include);
	}

	private static final String GROOVY_SCRIPT_CODE_BASE = "/groovy/script";

}
