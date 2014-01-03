#!/bin/bash
set -e

HEADER=$(cat <<HERE
// Copyright (c) 2013 Andrew Hayden. All rights reserved.
// Use of this source code is governed by an Apache 2.0 license that can be
// found in the LICENSE.md file.
// Source: https://github.com/andrewhayden/uopt4j
HERE
)

# Blow away old stuff
echo  "Cleaning"
rm -rf altsrc-compact altsrc-minified /tmp/uopt4j
mkdir altsrc-compact
mkdir altsrc-minified
mkdir -p /tmp/uopt4j


echo "Building source and minification tool"
javac -d /tmp/uopt4j -source 1.5 -target 1.5 src/MicroOptions.java example/JavaMinifyTool.java
echo "Running minification tool on source"
java -cp /tmp/uopt4j JavaMinifyTool --header "$HEADER" --in src/MicroOptions.java --out altsrc-compact/MicroOptions.java --mode compact
java -cp /tmp/uopt4j JavaMinifyTool --header "$HEADER" --in src/MicroOptions.java --out altsrc-minified/MicroOptions.java --mode min --wrap-at 80


echo "Verifying that minified code compiles"
rm -rf /tmp/uopt4j
mkdir -p /tmp/uopt4j
javac -d /tmp/uopt4j -source 1.5 -target 1.5 altsrc-compact/MicroOptions.java
rm -rf /tmp/uopt4j
mkdir -p /tmp/uopt4j
javac -d /tmp/uopt4j -source 1.5 -target 1.5 altsrc-minified/MicroOptions.java


echo "Building documentation"
rm -rf javadoc
mkdir javadoc
javadoc -sourcepath src -source 1.5 -quiet -windowtitle "uopt4j documentation" -d javadoc src/MicroOptions.java

echo "Building release JAR"
rm -rf release
mkdir release
jar cf release/uopt4j.jar -C /tmp/uopt4j/ .


echo "Done"
