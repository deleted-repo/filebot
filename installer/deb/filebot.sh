#!/bin/sh
FILEBOT_HOME="/usr/share/filebot"

if [ -z "$HOME" ]; then
	echo '$HOME must be set'
	exit 1
fi

# select application data folder
APP_DATA="$HOME/.filebot"
LIBRARY_PATH="$FILEBOT_HOME/lib"

java -Dapplication.deployment=deb -Dnet.filebot.Archive.extractor=SevenZipExecutable @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
