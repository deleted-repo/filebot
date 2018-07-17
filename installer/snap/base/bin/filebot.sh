#!/bin/sh
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

export JAVA_HOME="$SNAP/jre"
export FILEBOT_HOME="$SNAP/filebot"

APP_DATA="$SNAP_USER_DATA"
LIBRARY_PATH="$FILEBOT_HOME/lib"

"$JAVA_HOME/bin/java" -Dapplication.deployment=snap -Dapplication.update=skip -Dnet.filebot.license="$SNAP_DATA/.license" -Dnet.filebot.AcoustID.fpcalc="$SNAP/usr/bin/fpcalc" @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
