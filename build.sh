#!/bin/bash
# Copyright 2014 Google Inc. All rights reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -e

HEADER=$(cat <<HERE
// Copyright 2014 Google Inc. All rights reserved.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
