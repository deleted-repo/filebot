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
APP_ROOT=`cd "$BIN/.." && pwd`

JAVA_HOME="$APP_ROOT/PlugIns/jre-@{java.version}.jre/Contents/Home"
LIBRARY_PATH="$APP_ROOT/MacOS"

# launch filebot
"$JAVA_HOME/bin/java" @{java.application.options} -Dapplication.deployment=app -Djava.awt.headless=true -Dapple.awt.UIElement=true -Djna.boot.library.path="$LIBRARY_PATH" -Djna.library.path="$LIBRARY_PATH" -Djava.library.path="$LIBRARY_PATH" -Dnet.filebot.AcoustID.fpcalc="$LIBRARY_PATH/fpcalc" $JAVA_OPTS -classpath "$APP_ROOT/Java/*" net.filebot.Main "$@"
