#!/bin/zsh
# Builds a signed Tziak release APK. Signing secrets live in ~/.tziak (not in the project).
set -e
source ~/.tziak/signing.env
export KEYSTORE_PATH="$HOME/.tziak/release.keystore"
export KEYSTORE_PASSWORD="$STORE_PASSWORD"
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd "$(dirname "$0")"
./gradlew :androidApp:assembleRelease --console=plain
ls -lh androidApp/build/outputs/apk/release/*.apk
