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
if uname -m | egrep "i386|i686|amd64|x86_64"; then
	ARCHIVE_EXTRACTOR="SevenZipNativeBindings"  # use lib7-Zip-JBinding.so
	MEDIA_PARSER="libmediainfo"                 # use libmediainfo
else
	# armv7l or aarch64
	ARCHIVE_EXTRACTOR="ApacheVFS"               # use Apache Commons VFS2
	MEDIA_PARSER="ffprobe"                      # use ffprobe
fi

# choose ffprobe executable
FFPROBE="/mnt/ext/opt/medialibrary/bin/ffprobe"

# select application data folder
APP_DATA="$QPKG_ROOT/data/$USER"
LIBRARY_PATH="$QPKG_ROOT/lib/$(uname -m)"

# start filebot
java -Dapplication.deployment=qpkg -Dnet.filebot.license="$QPKG_ROOT/data/.license" -Dnet.filebot.media.parser="$MEDIA_PARSER" -Dnet.filebot.media.ffprobe="$FFPROBE" -Dnet.filebot.Archive.extractor="$ARCHIVE_EXTRACTOR" -Djava.awt.headless=true @{java.application.options} @{linux.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$QPKG_ROOT/jar/filebot.jar" "$@"
