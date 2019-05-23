#!/bin/sh
CONF="/etc/config/qpkg.conf"
QPKG_NAME="filebot"
QPKG_ROOT=$(/sbin/getcfg $QPKG_NAME Install_Path -f $CONF)


# make sure required environment variables are set
if [ -z "$USER" ]; then
	export USER=`whoami`
fi


# add package lib folder to library path
PACKAGE_LIBRARY_PATH="$QPKG_ROOT/lib/$(uname -m)"

# add fpcalc to the $PATH by default
export PATH="$PATH:$PACKAGE_LIBRARY_PATH"

# force JVM language and encoding settings
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

# use ffprobe executable
MEDIA_PARSER="ffprobe"
FFPROBE="/mnt/ext/opt/medialibrary/bin/ffprobe"

# use 7z and unrar executables
ARCHIVE_EXTRACTOR="ShellExecutables"
P7ZIP="/usr/local/sbin/7z"
UNRAR="/usr/local/sbin/unrar"

# select application data folder
APP_DATA="$QPKG_ROOT/data/$USER"
LIBRARY_PATH="$PACKAGE_LIBRARY_PATH:$LD_LIBRARY_PATH"

# make sure transitive dependencies can be loaded
export LD_LIBRARY_PATH="$LIBRARY_PATH"

# start filebot
java -Dapplication.deployment=qpkg -Dnet.filebot.license="$QPKG_ROOT/data/.license" -Dnet.filebot.media.parser="$MEDIA_PARSER" -Dnet.filebot.media.ffprobe="$FFPROBE" -Dnet.filebot.archive.extractor="$ARCHIVE_EXTRACTOR" -Dnet.filebot.archive.7z="$P7ZIP" -Dnet.filebot.archive.unrar="$UNRAR" -Djava.awt.headless=true @{java.application.options} @{linux.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$QPKG_ROOT/jar/filebot.jar" "$@"
