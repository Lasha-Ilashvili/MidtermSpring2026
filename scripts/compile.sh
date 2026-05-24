#!/usr/bin/env sh
set -eu

rm -rf out
mkdir -p out
javac -d out -sourcepath src src/Main.java
