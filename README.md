# Tziak

Tziak is a fork of [Metrolist](https://github.com/MetrolistGroup/Metrolist), a free and open-source YouTube Music client, built on Metrolist 14. It focuses on Android Auto reliability and a few defaults. Maintained by Andreas Tziakouris.

Tziak is an independent project. It is not affiliated with, or endorsed by, the Metrolist team, YouTube or Google.

## What is different from Metrolist

**Android Auto**
- The car shows a single Playlists screen, listed A–Z, with large artwork.
- The music service keeps running when Android refuses a foreground start, so the screen does not go black and the play button keeps working.

**App**
- New defaults: High audio quality, Big grid size, auto playlists off (except Downloaded), pause when media is muted on.
- A permanent Downloads entry in the Library, with a way to remove one song or all downloads.
- A debug log you can switch on (Settings → Privacy → Keep debug log) and share. It is off by default.
- The updater checks this project's releases and verifies a SHA-256 checksum. Metrolist's changelog screen is removed.
- New name, package id and icon.

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
