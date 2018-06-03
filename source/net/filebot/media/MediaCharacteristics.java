package net.filebot.media;

import java.time.Duration;

public interface MediaCharacteristics {

	String getVideoCodec();

	String getAudioLanguage();

	String getSubtitleCodec();

	Duration getDuration();

	float getFrameRate();

}
