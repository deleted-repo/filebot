#!/bin/sh
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

APP_DATA="$SNAP_USER_DATA/data"
LIBRARY_PATH="$SNAP/filebot/lib:$SNAP/usr/lib/x86_64-linux-gnu"

"$SNAP/usr/lib/jvm/java-8-openjdk-amd64/bin/java" -Dapplication.deployment=snap -Dapplication.update=skip -Dnet.filebot.archive.extractor=ShellExecutables -Dnet.filebot.archive.7z="$SNAP/usr/bin/7z" -Dnet.filebot.archive.unrar="$SNAP/usr/bin/unrar-nonfree" -Dnet.filebot.AcoustID.fpcalc="$SNAP/usr/bin/fpcalc" @{java.application.options} @{linux.application.options} @{linux.desktop.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$SNAP/filebot/jar/filebot.jar" "$@"
