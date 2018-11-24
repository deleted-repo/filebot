#!/bin/sh
FILEBOT_HOME="/usr/share/filebot"

if [ -z "$HOME" ]; then
	echo '$HOME must be set'
	exit 1
fi

# select application data folder
APP_DATA="$HOME/.filebot"
LIBRARY_PATH="$FILEBOT_HOME/lib/$(uname -m):/usr/lib/$(uname -m)-linux-gnu/jni"
MODULE_PATH="/usr/share/openjfx/lib"

java -Dapplication.deployment=deb --module-path "$MODULE_PATH" --add-modules ALL-MODULE-PATH -Dnet.filebot.Archive.extractor=SevenZipExecutable @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
