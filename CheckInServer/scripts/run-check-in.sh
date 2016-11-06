#!/bin/bash

if [ $# -eq 1 ] && [ "$1" == "-h" ] ; then
	echo "Usage: $0 [portname]"
	echo "  e.g., $0 /dev/ttyACM0"
	exit 1
fi

CHECKIN_PROJ_DIR=/home/brian/git/cyborgs-check-in/CheckInServer
export CLASSPATH=${CHECKIN_PROJ_DIR}/bin:${CHECKIN_PROJ_DIR}/lib/jssc-2.8.0.jar
java org.cyborgs3335.checkin.MainApp $@
