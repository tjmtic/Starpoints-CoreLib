# Starpoints-CoreLib

The domain core of **Starpoints**, a night-sky app for Android and iOS: point the phone at the sky
and it draws the stars, constellations and names that are there, from a bundled catalog and the
phone's orientation, whatever the weather or the light. This repo is the part with the ideas in it;
the app itself (UI, platform adapters, builds and releases) lives in a private repo.

Pure Kotlin Multiplatform, no UI and no platform types. It follows a hexagonal layout: the core
holds the model, the use cases and the **ports** (interfaces for orientation, location, time,
storage, the catalog source); the app implements those ports with adapters over the ViewPoint
libraries and wires them together. Dependencies point inward: nothing here knows which library
sits behind a port.

## What is in it

| Area | What |
|---|---|
| `math/` | Angles, time (Julian date, sidereal time), coordinates (equatorial → horizontal, precession, refraction), angular separation |
| catalog | The bundled star catalog's binary format and its reader; magnitude limits by field of view |
| projection | The camera model: look direction + field of view → screen positions, frustum culling, tap hit-testing |
| presenter | UI-free sky state for the app's screens to render |
| `ports/` | What the core needs from the platform, as interfaces |

The astronomy follows Jean Meeus, *Astronomical Algorithms* (2nd ed.); tests check the book's
worked examples. The model never computes astronomy: anything written in words is built from
facts this library computed.

## Build and test

```bash
./gradlew ktfmtCheck :detekt-rules:test detekt :core-lib:jvmTest
```

```bash
./gradlew :core-lib:iosSimulatorArm64Test
```

Targets: Android, JVM, iosArm64, iosSimulatorArm64. Kotlin 2.4.10, AGP 8.13.2 (in lockstep with
the apps that build this as a composite). Style: ktfmt (kotlinlang) and detekt at zero issues.

## Using it

A sibling checkout (`../Starpoints-CoreLib`) is compiled into the app as a composite build.
Tagged versions (`vX.Y.Z`) publish `com.abyxcz.starpoints.core:core-lib` to GitHub Packages.

## How it is built

Work arrives as issues on the Starpoints project board and is written by a local model through
the studio's edit loop: one issue at a time, a PR into `develop`, merged when CI is green and its
review step accepts. `main` moves only when a person merges the `develop` → `main` promotion PR.
