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
WORKING_DIR=`pwd`
PRG_DIR=`dirname "$PRG"`
FILEBOT_HOME=`cd "$PRG_DIR" && pwd`


# add package lib folder to library path
PACKAGE_LIBRARY_PATH="$FILEBOT_HOME/lib/$(uname -m)"

# add 3rd party packages to $LD_LIBRARY_PATH by default
SYNO_LIBRARY_PATH="/usr/local/mediainfo/lib:/usr/local/chromaprint/lib"

# add fpcalc to the $PATH by default
export PATH="$PATH:/usr/local/chromaprint/bin"


# restore original working dir (which may be /root and yield permission denied)
if [ -x "$WORKING_DIR" ]; then
	cd "$WORKING_DIR"
else
	cd "/volume1"
fi


# make sure required environment variables are set
if [ -z "$USER" ]; then
	export USER=`whoami`
fi

# force JVM language and encoding settings
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

# choose extractor
EXTRACTOR="ApacheVFS"                   # use Apache Commons VFS2 with junrar plugin
# EXTRACTOR="SevenZipExecutable"        # use the 7z executable
# EXTRACTOR="SevenZipNativeBindings"    # use the lib7-Zip-JBinding.so native library

# choose media parser
MEDIA_PARSER="libmediainfo"             # use libmediainfo
# MEDIA_PARSER="ffprobe"                # use ffprobe

# choose ffprobe executable
FFPROBE="/volume1/@appstore/MediaServer/bin/ffprobe"

# select application data folder
APP_DATA="$FILEBOT_HOME/data/$USER"
LIBRARY_PATH="$SYNO_LIBRARY_PATH:$PACKAGE_LIBRARY_PATH"

# start filebot
java -Dapplication.deployment=spk -Dnet.filebot.media.parser="$MEDIA_PARSER" -Dnet.filebot.media.ffprobe="$FFPROBE" -Dnet.filebot.Archive.extractor="$EXTRACTOR" -Djava.awt.headless=true @{java.application.options} @{linux.application.options} @{linux.portable.application.options} $JAVA_OPTS $FILEBOT_OPTS -jar "$FILEBOT_HOME/jar/filebot.jar" "$@"
