# Tziak

Tziak is a fork of [Metrolist](https://github.com/MetrolistGroup/Metrolist), a free and open-source YouTube Music client, built on Metrolist 14. It focuses on Android Auto reliability and a few defaults. Maintained by Andreas Tziakouris.

Tziak is an independent project. It is not affiliated with, or endorsed by, the Metrolist team, YouTube or Google.

## What is different from Metrolist

**Android Auto**
- The car shows a single Playlists screen, listed A–Z, with large artwork grid covers.
- YouTube's own "Liked Music" and "Episodes for Later" lists are hidden from the car list (they stay in the phone app).
- Starting the app from the car before the phone app has been opened works: the YouTube language and country are set at start-up.

**App**
- Defaults: High audio quality, Big grid size, auto playlists off (except Downloaded), pause when media is muted on, and a full-screen player look (full-screen artwork, animated gradient background, animated covers).
- A debug log you can switch on (Settings → Privacy → Keep debug log) and share. It is off by default.
- The updater checks this project's releases and verifies a SHA-256 checksum before installing.
- New name and icon.

## Building

You need Android Studio's JDK (version 21) and the Android SDK.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :androidApp:assembleRelease
```

For a signed release build, create your own keystore and set `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` (see `build-release.sh`). Never commit your keystore or passwords.

## License and credits

Tziak is licensed under the [GNU General Public License v3.0](LICENSE), the same as Metrolist. The source code of every APK distributed from this project is available in this repository.

Thanks to the Metrolist contributors, whose work this is built on. The upstream project is at https://github.com/MetrolistGroup/Metrolist.
