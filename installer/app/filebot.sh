#!/bin/sh
PRG="$0"

# resolve relative symlinks
while [ -h "$PRG" ] ; do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '.*-> \(.*\)$'`
	if expr "$link" : '/.*' > /dev/null; then
		PRG="$link"
	else
		PRG="`dirname "$PRG"`/$link"
	fi
done

# get canonical path
BIN=`dirname "$PRG"`
FILEBOT_HOME=`cd "$BIN/.." && pwd`
JAVA_HOME="$FILEBOT_HOME/PlugIns/jre-@{java.version}.jre/Contents/Home"

# select application data folder
APP_DATA="$HOME/.filebot"
LIBRARY_PATH="$FILEBOT_HOME/MacOS"

# start filebot
"$JAVA_HOME/bin/java" -Dapplication.deployment=app -Dapple.awt.UIElement=true -Dnet.filebot.AcoustID.fpcalc="$LIBRARY_PATH/fpcalc" @{java.application.options} @{linux.application.options} $JAVA_OPTS $FILEBOT_OPTS -classpath "$FILEBOT_HOME/Java/*" @{main.class} "$@"
