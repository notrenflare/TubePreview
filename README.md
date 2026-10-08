<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" alt="TubePreview logo" />
</p>

# TubePreview

Preview your thumbnails in a YouTube-style home feed before you publish.
TubePreview is a native Android app built with Kotlin and Jetpack Compose.
No YouTube account or API key is required.

## What you can do

- Add a thumbnail and an optional channel photo.
- Customize the channel name, video title, views, and duration.
- See your video alongside sample videos and adjust its placement in the feed.
- Switch between light and dark themes.
- Reset your preview whenever you want a fresh start.

TubePreview is an independent preview tool and is not affiliated with YouTube or Google.
It does not upload videos or modify your YouTube channel.

## Build and run

1. Install Android Studio with JDK 21 and Android SDK Platform 37.
2. Clone this repository and open its root folder in Android Studio.
3. Let Gradle sync finish, select a device or emulator, and run the `app` module.

Android 7.0 (API 24) or newer is required. The application ID is
`com.renflare.tubepreview`. The Gradle wrapper manages the project's Gradle version.

To build from a terminal with the Android SDK configured:

```sh
bash ./gradlew :app:assembleDebug
```

To run the unit tests and lint:

```sh
bash ./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease
```

## Privacy and security

Imported images and preview settings stay in private app storage. There is no
sign-in or backend service, and imported images are not uploaded. Sample feed photos
load from Unsplash over HTTPS. App data is excluded from Android backup and device
transfer; uninstalling the app removes your local preview settings.

See [SECURITY.md](SECURITY.md) for vulnerability reporting and repeatable checks,
and [SECURITY_AUDIT.md](SECURITY_AUDIT.md) for the dated security review and its limits.
GitHub Actions checks builds, unit tests, lint, secrets, and resolved dependency
advisories. Dependabot checks for dependency updates weekly.

## Release builds

Release signing credentials are supplied externally through `KEYSTORE_PATH`,
`STORE_PASSWORD`, and `KEY_PASSWORD`. Keep the keystore and passwords outside Git.
Without a release keystore, the release APK is unsigned. Source publication on
GitHub does not publish an app to Google Play or create a downloadable signed release.

## Development

Production code lives in `app/src/main/java/com/renflare/tubepreview/`, with data,
UI components/screens, theming, and the ViewModel in separate packages. Local tests
live in `app/src/test/`, and device tests in `app/src/androidTest/`.

After making changes, review the diff, commit it, and push to your branch. Check the
repository's Actions tab to confirm that the build and security checks pass.

Made with ❤️ by **renflare**.
