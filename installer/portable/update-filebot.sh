#!/bin/sh
PRG="$0"

# resolve relative symlinks
while [ -h "$PRG" ]; do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '.*-> \(.*\)$'`
	if expr "$link" : '/.*' > /dev/null; then
		PRG="$link"
	else
		PRG="`dirname "$PRG"`/$link"
	fi
done

# make it fully qualified
PRG_DIR=`dirname "$PRG"`
FILEBOT_HOME=`cd "$PRG_DIR" && pwd`


# update core application files
PACKAGE_NAME="CHANGES.tar.xz.gpg"
PACKAGE_FILE="$FILEBOT_HOME/$PACKAGE_NAME"
PACKAGE_URL="https://get.filebot.net/filebot/latest/$PACKAGE_NAME"

echo "Update $PACKAGE_FILE"
HTTP_CODE=`curl -L -o "$PACKAGE_FILE" -z "$PACKAGE_FILE" --retry 5 "$PACKAGE_URL" -w "%{http_code}"`

if [ $HTTP_CODE -ne 200 ]; then
	echo "$HTTP_CODE NO UPDATE"
	exit 1
fi


# initialize gpg
GPG_HOME="$FILEBOT_HOME/data/.gpg"

if [ ! -d "$GPG_HOME" ]; then
	mkdir -p "$GPG_HOME" && chmod 700 "$GPG_HOME" && gpg --homedir "$GPG_HOME" --import "$FILEBOT_HOME/maintainer.pub"
fi

# verify signature and extract jar
gpg --batch --yes --homedir "$GPG_HOME" --trusted-key "4E402EBF7C3C6A71" --decrypt "$PACKAGE_FILE" | tar -xJvf "-"
