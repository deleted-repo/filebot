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

# launch filebot
"$APP_ROOT"/PlugIns/*.jre/Contents/Home/bin/java @{java.application.options} -Dapplication.deployment=app -Djava.awt.headless=true -Dapple.awt.UIElement=true -Djna.boot.library.path="$APP_ROOT/MacOS" -Djna.library.path="$APP_ROOT/MacOS" -Djava.library.path="$APP_ROOT/MacOS" -Dnet.filebot.AcoustID.fpcalc="$APP_ROOT/MacOS/fpcalc" $JAVA_OPTS -classpath "$APP_ROOT/Java/*" net.filebot.Main "$@"
