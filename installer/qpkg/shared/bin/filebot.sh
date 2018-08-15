#!/bin/sh
CONF="/etc/config/qpkg.conf"
QPKG_NAME="filebot"
QPKG_ROOT=$(/sbin/getcfg $QPKG_NAME Install_Path -f $CONF)


# make sure required environment variables are set
if [ -z "$USER" ]; then
	export USER=`whoami`
fi

# force JVM language and encoding settings
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

# choose archive extractor / media characteristics parser
ARCHIVE_EXTRACTOR="ApacheVFS"
MEDIA_PARSER="ffprobe"

# choose ffprobe executable
FFPROBE="/mnt/ext/opt/medialibrary/bin/ffprobe"

# select application data folder
APP_DATA="$QPKG_ROOT/data/$USER"
LIBRARY_PATH="$QPKG_ROOT/lib/$(uname -m)"

# start filebot
java -Dapplication.deployment=qpkg -Dnet.filebot.license="$QPKG_ROOT/data/.license" -Dnet.filebot.media.parser="$MEDIA_PARSER" -Dnet.filebot.media.ffprobe="$FFPROBE" -Dnet.filebot.Archive.extractor="$ARCHIVE_EXTRACTOR" -Djava.awt.headless=true @{java.application.options} @{linux.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$QPKG_ROOT/jar/filebot.jar" "$@"
