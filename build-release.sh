#!/bin/zsh
# Builds a signed Tziak release APK. Signing secrets live in ~/.tziak (not in the project).
set -e
source ~/.tziak/signing.env
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd "$(dirname "$0")"
./gradlew :app:assembleFossRelease --console=plain
ls -lh app/build/outputs/apk/foss/release/*.apk
