#!/bin/sh
APP_ROOT="/usr/share/filebot"

if [ -z "$HOME" ]; then
	echo '$HOME must be set'
	exit 1
fi

# select application data folder
APP_DATA="$HOME/.filebot"

java @{java.application.options} -Dapplication.deployment=deb -Djdk.gtk.version=3 -DuseGVFS=true -Dnet.filebot.gio.GVFS="$XDG_RUNTIME_DIR/gvfs" -Dapplication.dir="$APP_DATA" -Djava.io.tmpdir="$APP_DATA/tmp" -Djna.boot.library.path="$APP_ROOT/lib" -Djna.library.path="$APP_ROOT/lib" -Djava.library.path="$APP_ROOT/lib" -Dnet.filebot.AcoustID.fpcalc="$APP_ROOT/lib/fpcalc" $JAVA_OPTS -classpath "$APP_ROOT/jar/*" @{main.class} "$@"
