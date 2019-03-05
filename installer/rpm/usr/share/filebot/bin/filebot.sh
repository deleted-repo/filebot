#!/usr/bin/sh
FILEBOT_HOME="/usr/share/filebot"

if [ -z "$HOME" ]; then
	echo '$HOME must be set'
	exit 1
fi

# select application data folder
APP_DATA="$HOME/.filebot"

# select libjnidispatch.so from lib or lib64
LIBRARY_PATH=/usr/lib*/jna

java -Dapplication.deployment=rpm -Djna.boot.library.name=jnidispatch -Dnet.filebot.archive.extractor=ShellExecutables @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
