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
PRG_DIR=`dirname "$PRG"`
FILEBOT_HOME=`cd "$PRG_DIR" && pwd`

# add package lib folder to library path
PACKAGE_LIBRARY_PATH="$FILEBOT_HOME/lib/$(uname -m)"

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

# select application data folder
APP_DATA="$FILEBOT_HOME/data"
LIBRARY_PATH="$PACKAGE_LIBRARY_PATH:$LD_LIBRARY_PATH"

# start filebot
java -Dapplication.deployment=portable -Dnet.filebot.media.parser="$MEDIA_PARSER" -Dnet.filebot.Archive.extractor="$EXTRACTOR" -Duser.home="$APP_DATA" -Djava.util.prefs.PreferencesFactory=net.filebot.util.prefs.FilePreferencesFactory -Dnet.filebot.util.prefs.file="$APP_DATA/prefs.properties" @{java.application.options} @{linux.application.options} @{linux.headless.application.options} $JAVA_OPTS $FILEBOT_OPTS -classpath "$FILEBOT_HOME/jar/*" @{main.class} "$@"
