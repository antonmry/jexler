#!/bin/bash

# I used this script to submit the cron4j library by Carlo Pelliccia
# via sonatype to maven central, with permission by the author until
# he finds the time to do so himself. Maybe someone finds it useful
# for submitting other 3rd party libraries to maven central :)

# Needs the following directory structure to work:
# - sonatype-cron4j.sh            This bash script
# - source/cron4j-<version>.zip:  The cron4j release to submit

# Takes the cron4j release ZIP file, extracts the jar and creates
# a javadoc and sources jar plus a pom, signs these, and bundles them
# into a bundle jar that can be uploaded to oss.sonatype.org.
# (Note that you need a sonatype account, a pgp key and more or less
# the permission by the author to do so :)

set -e
trap "printf \"\e[31;1mFAILED\e[0m\n\"" EXIT

if [ "$#" != "1" ]; then
  echo "usage: $0 <version>"
  exit 1
fi

VER=$1
echo "VER: $VER"

# cd to directory with this script
cd "${0%/*}"
ROOT=`pwd`
SOURCE="$ROOT/source"
TARGET="$ROOT/target"
echo "SOURCE: $SOURCE"
echo "TARGET: $TARGET"

rm -rf "$TARGET"
mkdir "$TARGET"

# unzip original distribution
unzip -q "$SOURCE/cron4j-$VER.zip" -d "$TARGET"
CRON4J="$ROOT/target/cron4j-$VER"

# normal jar
CRON4J_JAR="cron4j-$VER.jar"
cp "$CRON4J/$CRON4J_JAR" "$TARGET"
unzip -q "$CRON4J/$CRON4J_JAR" -d "$TARGET/$CRON4J_JAR-unzipped"

# javadoc jar
CRON4J_DOC_JAR="cron4j-$VER-javadoc.jar"
cd "$CRON4J/doc/api"
jar cf "$TARGET/$CRON4J_DOC_JAR" .
unzip -q "$TARGET/$CRON4J_DOC_JAR" -d "$TARGET/$CRON4J_DOC_JAR-unzipped"

# sources jar
CRON4J_SRC_JAR="cron4j-$VER-sources.jar"
cd "$CRON4J/src"
jar cf "$TARGET/$CRON4J_SRC_JAR" .
unzip -q "$TARGET/$CRON4J_SRC_JAR" -d "$TARGET/$CRON4J_SRC_JAR-unzipped"

# pom
CRON4J_POM="cron4j-$VER.pom"
cat >"$TARGET/$CRON4J_POM" <<EOF
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>it.sauronsoftware.cron4j</groupId>
  <artifactId>cron4j</artifactId>
  <version>$VER</version>
  <packaging>jar</packaging>

  <name>cron4j</name>
  <description>cron4j is a scheduler for the Java platform which is very similar to the UNIX cron daemon.</description>
  <url>http://www.sauronsoftware.it/projects/cron4j/</url>

  <licenses>
    <license>
      <name>GNU General Lesser Public License (LGPL) version 2.1</name>
      <url>http://www.gnu.org/licenses/lgpl-2.1.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>

  <scm>
    <url>http://sourceforge.net/p/cron4j/code</url>
    <connection>svn://svn.code.sf.net/p/cron4j/code/</connection>
  </scm>

  <developers>
    <developer>
      <id>cpelliccia</id>
      <name>Carlo Pelliccia</name>
      <email>cpelliccia@sauronsoftware.it</email>
    </developer>
  </developers>

  <dependencies>
    <!-- cron4j has no maven dependencies -->
  </dependencies>
</project>
EOF
cp "$TARGET/$CRON4J_POM" "$TARGET/pom.xml"

# sign artefacts
CRON4J_ARTS="$CRON4J_JAR $CRON4J_DOC_JAR $CRON4J_SRC_JAR $CRON4J_POM"
cd $TARGET
for ART in $CRON4J_ARTS; do
  gpg -ab "$ART"
done

# show generated files
#ls -al *.jar *.pom *.asc

# bundle
CRON4J_BUNDLE_JAR="bundle-$VER.jar"
for ART in $CRON4J_ARTS; do
  FILES="$FILES $ART $ART.asc"
done
jar cf "$CRON4J_BUNDLE_JAR" $FILES
unzip -q "$CRON4J_BUNDLE_JAR" -d "$CRON4J_BUNDLE_JAR-unzipped"

# show generated files
ls -l "$CRON4J_BUNDLE_JAR-unzipped"

printf "\e[32;1mSUCCESS\e[0m\n"

trap - EXIT
