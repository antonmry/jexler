#!/bin/bash

# I use this script to submit the jexler-core library via sonatype
# to the maven central repository, so it's essentially for me only
# to use, but maybe someone finds this script useful.

# hints to me:
# - copy M2_HOME/conf/settings.xml to ~/.m2/settings.xml and add
#   the following:
#     <server>
#       <id>sonatype-nexus-staging</id>
#       <username>jexler</username>
#       <password>(my real password)</password>
#     </server>
# - build jexler including the 'pom' task
# - after submitting, close and promote using the web gui at
#   oss.sonatype.org to maven central:
#     https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide

set -e
trap "printf \"\e[31;1mFAILED\e[0m\n\"" EXIT

if [ $# -eq 0 ]; then
  echo "usage: $0 <version>"
  exit 1
fi

VERSION=$1
echo
echo "VERSION: $VERSION"

# cd to directory with this script
cd "${0%/*}"
ROOT=`pwd`
SOURCE="$ROOT/../../jexler-core/build/libs"
echo "SOURCE: $SOURCE"
cd "$SOURCE"

# filenames
CORE_NAME=jexler-core-$VERSION
CORE_POM=$CORE_NAME.pom
CORE_JAR=$CORE_NAME.jar
CORE_SRC_JAR=$CORE_NAME-sources.jar
CORE_DOC_JAR=$CORE_NAME-javadoc.jar

echo
echo "mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_JAR"
echo "mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_SRC_JAR -Dclassifier=sources"
echo "mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_DOC_JAR -Dclassifier=javadoc"

echo
printf "Really publish artefacts (y/N)? "
read ANSWER
if [ "$ANSWER" == "y" ]; then
  echo "Publishing artefacts..."
  mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_JAR
  mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_SRC_JAR -Dclassifier=sources
  mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$CORE_POM -Dfile=$CORE_DOC_JAR -Dclassifier=javadoc
  printf "\e[32;1mSUCCESS\e[0m\n"
else
  printf "\e[34;1mSKIPPED\e[0m\n"
fi

echo

trap - EXIT
