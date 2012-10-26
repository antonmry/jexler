#!/bin/bash
# run jexler in target directory

set -e
trap "echo; printf \"\e[31;1mFAILED\e[0m\n\"" EXIT

cd "${0%/*}"
JEXLER_DIR=`pwd`

# assemble classpath
CP="$JEXLER_DIR/`ls target/jexler-*.jar`"
CP="$CP:`cat target/classpath`"
#echo "$CP"

java -cp "$CP" net.jexler.JexlerMain

trap - EXIT

