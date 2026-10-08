<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" alt="TubePreview logo" />
</p>

# TubePreview

**Preview before you publish.**

See how your thumbnails, titles, and channel branding look in a YouTube-style
home feed. Built for Android with Kotlin and Jetpack Compose, with no YouTube
sign-in or API key required.

## What you can do

- Add a thumbnail and an optional channel photo.
- Customize the channel name, video title, views, and duration.
- See your video alongside sample videos and adjust its placement in the feed.
- Switch between light and dark themes.
- Reset your preview whenever you want a fresh start.

TubePreview is an independent preview tool and is not affiliated with YouTube or Google.
It does not upload videos or modify your YouTube channel.

## Screenshots

<table>
  <tr>
    <th>Get started</th>
    <th>Dark feed</th>
    <th>Light feed</th>
    <th>Customize</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/onboarding.png" width="210" alt="Fresh onboarding with the TubePreview logo and renflare channel name" /></td>
    <td><img src="docs/screenshots/home-dark.png" width="210" alt="Default thumbnail preview in a dark home feed" /></td>
    <td><img src="docs/screenshots/home-light.png" width="210" alt="The same default preview in a light home feed" /></td>
    <td><img src="docs/screenshots/customize.png" width="210" alt="Channel and video customization with an empty thumbnail picker" /></td>
  </tr>
</table>

Captured on a Samsung Galaxy A55 after clearing app data. The feed uses sample
content; the preview card stays empty until you choose a thumbnail.

## Build and run

1. Install Android Studio with JDK 21 and Android SDK Platform 37.
2. Clone `notrenflare/TubePreview` while signed in to an account with repository access,
   then open its root folder in Android Studio.
3. Let Gradle sync finish, select a device or emulator, and run the `app` module.

With GitHub CLI:

```sh
gh repo clone notrenflare/TubePreview
cd TubePreview
```

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

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development workflow and checks.
Build and security results are available in [GitHub Actions](https://github.com/notrenflare/TubePreview/actions).

Made with ❤️ by [**renflare**](https://github.com/notrenflare).
