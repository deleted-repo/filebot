#!/bin/sh

case "$1" in
	start)
		exit 0
	;;

	stop)
		exit 0
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
