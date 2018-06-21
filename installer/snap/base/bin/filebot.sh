#!/bin/sh
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

export JAVA_HOME="$SNAP/jre"
export FILEBOT_HOME="$SNAP/filebot"

APP_DATA="$SNAP_USER_DATA/data"
LIBRARY_PATH="$FILEBOT_HOME/lib"

"$JAVA_HOME/bin/java" -Dapplication.deployment=snap -Dapplication.update=skip -Dnet.filebot.AcoustID.fpcalc="$SNAP/usr/bin/fpcalc" -Duser.home="$SNAP_USER_DATA" -Djava.util.prefs.PreferencesFactory=net.filebot.util.prefs.FilePreferencesFactory -Dnet.filebot.util.prefs.file="$APP_DATA/prefs.properties" @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} $JAVA_OPTS $FILEBOT_OPTS -classpath "$FILEBOT_HOME/jar/*" @{main.class} "$@"
