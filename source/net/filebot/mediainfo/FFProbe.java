package net.filebot.mediainfo;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Map;

import com.cedarsoftware.util.io.JsonReader;

public class FFProbe {

	public String getFFProbeCommand() {
		return System.getProperty("net.filebot.mediainfo.ffprobe", "ffprobe");
	}

	public List<Map<String, Object>> streams(File file) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(getFFProbeCommand(), "-show_streams", "-print_format", "json", "-v", "error", file.getCanonicalPath());

		processBuilder.directory(file.getParentFile());
		processBuilder.redirectError(Redirect.INHERIT);

		Process process = processBuilder.start();

		// parse process standard output
		Map<String, Object> json = (Map<String, Object>) JsonReader.jsonToJava(process.getInputStream(), singletonMap(JsonReader.USE_MAPS, true));
		List<Map<String, Object>> streams = (List) asList((Object[]) json.get("streams"));

		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new IOException(String.format("%s failed with exit code %d", processBuilder.command(), exitCode));
		}

		return streams;
	}

}
