# Tziak on Metrolist v14 — design

Approved by the owner on 2026-11-16 (short design; small-project size, several steps).

## Intent
Metrolist 14.0.0 is a ground-up Kotlin Multiplatform rewrite. Tziak (currently 13.7.0-tziak.5, built on the old single `app` module) is rebuilt on the v14.0.0 code, re-applying only the changes the owner chose. The owner mainly uses Tziak on Android Auto in the car and offline downloads on trips.

## Goals
- Branch `tziak-v14` from the upstream tag v14.0.0 in worktree `../Tziak-v14`. The old `tziak` branch / folder `../Tziak` stays untouched as the fallback (tziak.5).
- Test build installs beside tziak.5: package `com.tziak.music.next`, name "Tziak Next". Switch to `com.tziak.music` / "Tziak" only when the owner is happy.
- Re-apply, in this order:
  1. Identity: name, package, wave icon (+ monochrome), credits (Andreas Tziakouris only, no email anywhere), README crediting Metrolist.
  2. Defaults (in shared `Settings.kt`): Android Auto shows only Playlists; grid size BIG; auto playlists off on the phone EXCEPT "Downloaded" stays visible; audio quality HIGH; pause when media is muted ON.
  3. Debug log: "Keep debug log" switch, OFF by default, Share debug log, crash report; the Timber file tree is reused.
  4. Android Auto: test v14's own implementation first. Ensure the service survives a refused foreground start (black screen / dead play button fix, only if v14 still has the problem), large artwork grid for playlists, playlists A–Z, no Liked/Downloaded tiles in the car's Playlists screen, no "Continue listening" entry (auto-resume on connect must keep working).
  5. Downloads: make it clear what was downloaded and how to remove it (v14 has a Downloads screen; verify it is easy to reach and that removing works; add an entry point if not).
  6. Updater: v14's update screen points at `tziakouris/Tziak` releases, keeps SHA-256 check and in-app install; remove Metrolist's changelog / "what's new" popup. Version scheme `14.0.0-tziak.N`.
- Local signed release APK with the existing key (`~/.tziak`) for car testing.

## Non-goals / anti-goals
- No publishing to GitHub for now (owner tests in the car first). No pushes.
- Not re-doing: Stop Android Auto shortcut, "Couldn't play this song" messages, battery/heat Diagnostics logger, the old downloads-on-main-thread fix, restart-on-kill features (never re-add; they caused a rebind loop).
- Do not change how the playlists are ordered inside a playlist (owner likes it as is).
- No email address of the owner in repo, commits or app. Commit identity: `Andreas Tziakouris <noreply@tziak.invalid>`.
- Desktop/iOS modules are left as upstream; only the Android app is built.

## Constraints
- GPL-3.0, keep upstream credits. JDK: Android Studio JBR 21. Signing key stays outside the repo.
- No shared git history with the old Tziak branch (history was rewritten), so everything is a manual port.
- v14 is a brand-new release; expect rough edges and verify each item by building and, where possible, running it.

## Approaches considered
1. **Fresh branch from the v14.0.0 tag, re-apply changes manually (chosen).** Smallest risk to the working tziak.5; clean diff against upstream.
2. Patch Metrolist's released v14 APK: rejected (cannot rebrand properly or change code).
3. Port v14 features back into the old code: rejected (far larger than re-applying our small set of changes).

## Left to the builder
Exact file locations in v14 for each change, how v14's Android Auto behaves with a refused foreground start, whether the Downloads screen needs a new entry point, build flavor details.
