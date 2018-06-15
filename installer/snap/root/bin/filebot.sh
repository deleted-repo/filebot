#!/bin/sh
export APP_ROOT="$SNAP/filebot"

export APP_DATA="$SNAP_USER_DATA/data"
export APP_CACHE="$SNAP_USER_DATA/cache"
export APP_PREFS="$SNAP_USER_DATA/prefs"

"$SNAP/jre/bin/java" -Djdk.gtk.version=2 @{java.application.options} -Dapplication.deployment=snap -Dapplication.update=skip -Dapplication.help=show -Djava.library.path="$APP_ROOT/lib" -Djna.library.path="$APP_ROOT/lib" -Djna.boot.library.path="$APP_ROOT/lib" -Dfile.encoding="UTF-8" -Dsun.jnu.encoding="UTF-8" -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -Dnet.filebot.UserFiles.fileChooser=JavaFX -DuseGVFS=true -Dnet.filebot.gio.GVFS="/run/user/$(id -u)/gvfs" -Duser.home="$SNAP_USER_DATA" -Dapplication.dir="$APP_DATA" -Dapplication.cache="$APP_CACHE/ehcache.disk.store" -Djava.io.tmpdir="$APP_CACHE/java.io.tmpdir" -Djava.util.prefs.userRoot="$APP_PREFS/user" -Djava.util.prefs.systemRoot="$APP_PREFS/system" -Dnet.filebot.AcoustID.fpcalc="$APP_ROOT/lib/fpcalc" $JAVA_OPTS -classpath "$APP_ROOT/jar/*" @{main.class} "$@"
