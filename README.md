# Tziak

Tziak is a fork of [Metrolist](https://github.com/MetrolistGroup/Metrolist), a free and open-source YouTube Music client for Android. It is built on Metrolist's code, with changes aimed at Android Auto reliability and a few defaults. Maintained by Andreas Tziakouris.

Tziak is an independent project. It is not affiliated with, or endorsed by, the Metrolist team, YouTube or Google.

## What is different from Metrolist

**Android Auto**
- Large artwork for playlists, albums and artists, shown as a grid.
- Playlists are listed A–Z, and the Liked/Downloaded tiles are removed from the Playlists screen.
- A "Continue listening" entry that resumes the current queue.
- The music service now keeps starting when Android 17 refuses a foreground start (for example when Android Auto connects from the background). Previously this left the service without a player, which caused a black screen and a play button that did nothing until the app was force-closed.
- Stuck retry and resolver state is reset when a new item is chosen from the car, and a "Couldn't play this song" message appears when a song fails.
- A "Stop Android Auto" launcher shortcut that opens Android Auto's app-info page.

**App**
- A shareable debug log: Settings → Privacy → Share debug log.
- New defaults: High audio quality, Big grid size, only Playlists visible in Android Auto, auto-playlists off, pause when media is muted on.
- The in-app updater and changelog are removed, because they point at Metrolist's releases.
- New name, package id (`com.tziak.music`) and icon.

## Installing

1. Download the APK and open it on your phone. Allow installing from the app you opened it in.
2. To use it in the car, Android Auto must show apps from outside the Play Store: open Android Auto settings, tap the version number about 10 times, open the Developer settings menu, and enable **Unknown sources**.
3. Sign in with your own YouTube account inside the app. No account data is included in the APK.

## Building

You need Android Studio's JDK (version 21) and the Android SDK.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleFossDebug
```

The APK is written to `app/build/outputs/apk/foss/debug/app-foss-debug.apk`.

For a signed release build, create your own keystore and set `STORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` (see `build-release.sh`), then run `./build-release.sh`. Never commit your keystore or passwords.

## License and credits

Tziak is licensed under the [GNU General Public License v3.0](LICENSE), the same as Metrolist. The source code of every APK distributed from this project is available in this repository.

Thanks to the Metrolist contributors, whose work this is built on. The upstream project is at https://github.com/MetrolistGroup/Metrolist.
