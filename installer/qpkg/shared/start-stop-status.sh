#!/bin/sh
CONF="/etc/config/qpkg.conf"
QPKG_NAME="filebot"
QPKG_ROOT=`/sbin/getcfg $QPKG_NAME Install_Path -f $CONF`


case "$1" in
	start)
		ENABLED=$(/sbin/getcfg $QPKG_NAME Enable -u -d FALSE -f $CONF)
		if [ "$ENABLED" != "TRUE" ]; then
			echo "$QPKG_NAME is disabled."
			exit 1
		fi

		# create /usr/bin/[package] program link
		ln -sf "$QPKG_ROOT/filebot.sh" "/usr/bin/filebot"
	;;

	stop)
		rm "/usr/bin/filebot"
	;;

	restart)
		$0 stop
		$0 start
	;;

	*)
		echo "Usage: $0 {start|stop|restart}"
		exit 1
esac


exit 0
