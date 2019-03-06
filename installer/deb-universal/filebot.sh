#!/bin/sh
FILEBOT_HOME="/usr/share/filebot"

if [ -z "$HOME" ]; then
	echo '$HOME must be set'
	exit 1
fi

# select application data folder
APP_DATA="$HOME/.filebot"

# select libjnidispatch.system.so from $(uname -m)-linux-gnu folder
LIBRARY_PATH=$(echo /usr/lib/*-linux-gnu*/jni | tr ' ' ':')

java -Dapplication.deployment=deb -Djna.boot.library.name=jnidispatch.system -Dnet.filebot.archive.extractor=ShellExecutables @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
