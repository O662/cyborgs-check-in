#!/bin/sh
CHECKIN_PROJ_DIR=/home/brian/git/cyborgs-check-in/CheckInServer
export CLASSPATH=${CHECKIN_PROJ_DIR}/bin:${CHECKIN_PROJ_DIR}/lib/jssc-2.8.0.jar
java org.cyborgs3335.checkin.rfidreader.JsscExamples $@
