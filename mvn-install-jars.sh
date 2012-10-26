#!/bin/bash
# install jars in the jars directory to local maven repository

set -e
trap "echo; printf \"\e[31;1mFAILED\e[0m\n\"" EXIT

cd "${0%/*}"

mvn install:install-file -Dfile=jars/cron4j-2.2.5.jar -DgroupId=net.sf.cron4j \
    -DartifactId=cron4j -Dversion=2.2.5 -Dpackaging=jar

echo - EXIT