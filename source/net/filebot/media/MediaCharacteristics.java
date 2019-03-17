package net.filebot.media;

import java.time.Duration;
import java.time.Instant;

public interface MediaCharacteristics extends AutoCloseable {

	String getVideoCodec();

	String getAudioCodec();

	String getAudioLanguage();

	String getSubtitleCodec();

	String getSubtitleLanguage();

	Duration getDuration();

	Integer getWidth();

	Integer getHeight();

	Double getBitRate();

	Float getFrameRate();

	String getTitle();

	Instant getCreationTime();

}
