package net.filebot.media;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.Execute.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class FFProbe implements MediaCharacteristics {

	public String getFFProbeCommand() {
		return System.getProperty("net.filebot.media.ffprobe", "ffprobe");
	}

	public String version() throws IOException {
		return execute(getFFProbeCommand(), "-show_program_version", "-hide_banner").toString().trim();
	}

	protected Map<String, Object> parse(File file) throws IOException, InterruptedException {
		String[] command = { getFFProbeCommand(), "-show_streams", "-show_format", "-print_format", "json", "-v", "error", file.getCanonicalPath() };

		CharSequence output = execute(command, file.getParentFile());

		// parse process standard output
		return (Map) JsonReader.jsonToJava(output.toString(), singletonMap(JsonReader.USE_MAPS, true));
	}

	private Map<String, Object> json;

	public synchronized FFProbe open(File file) throws IOException, InterruptedException {
		json = parse(file);
		return this;
	}

	@Override
	public synchronized void close() {
		json = null;
	}

	@Override
	public String getVideoCodec() {
		return getString("video", "codec_name");
	}

	@Override
	public String getAudioCodec() {
		return getString("audio", "codec_name");
	}

	@Override
	public String getAudioLanguage() {
		return getString("audio", "tags", "language");
	}

	@Override
	public String getSubtitleCodec() {
		return getString("subtitle", "codec_name");
	}

	@Override
	public String getSubtitleLanguage() {
		return getString("subtitle", "tags", "language");
	}

	@Override
	public Duration getDuration() {
		long d = (long) Double.parseDouble(getFormat().get("duration").toString()) * 1000;
		return Duration.ofMillis(d);
	}

	@Override
	public Integer getWidth() {
		return getInteger("video", "width");
	}

	@Override
	public Integer getHeight() {
		return getInteger("video", "height");
	}

	@Override
	public Double getBitRate() {
		return Double.parseDouble(getFormat().get("bit_rate").toString());
	}

	@Override
	public Float getFrameRate() {
		return find("video", "avg_frame_rate").map(fps -> {
			switch (fps) {
			case "500/21":
				return 23.976f; // normalize FPS value (using MediaInfo standards)
			default:
				return Float.parseFloat(fps);
			}
		}).get();
	}

	@Override
	public String getTitle() {
		return getTag("title").orElse(null);
	}

	@Override
	public Instant getCreationTime() {
		return getTag("creation_time").map(this::parseDateTime).orElse(null);
	}

	private Instant parseDateTime(String s) {
		return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")).toInstant(ZoneOffset.UTC);
	}

	public Map<String, Object> getFormat() {
		return (Map) json.get("format");
	}

	public Map<String, Object> getTags() {
		return (Map<String, Object>) getFormat().get("tags");
	}

	public Optional<String> getTag(String tag) {
		return Optional.ofNullable(getTags()).map(m -> (String) m.get(tag));
	}

	public List<Map<String, Object>> getStreams() {
		return (List) asList((Object[]) json.get("streams"));
	}

	protected String getString(String streamKind, String key) {
		return stream(streamKind, key).map(Objects::toString).collect(joining(" "));
	}

	protected String getString(String streamKind, String objectKey, String valueKey) {
		return stream(streamKind, objectKey).map(t -> ((Map) t).get(valueKey)).map(Objects::toString).collect(joining(" / "));
	}

	protected Stream<Object> stream(String streamKind, String property) {
		return getStreams().stream().filter(s -> streamKind.equals(s.get("codec_type"))).map(s -> s.get(property)).filter(Objects::nonNull);
	}

	protected Integer getInteger(String streamKind, String property) {
		return find(streamKind, property).map(Integer::parseInt).get();
	}

	protected Optional<String> find(String streamKind, String property) {
		return stream(streamKind, property).map(Objects::toString).findFirst();
	}

	@Override
	public String toString() {
		return JsonWriter.objectToJson(json);
	}

}
