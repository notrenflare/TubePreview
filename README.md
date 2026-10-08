<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" alt="TubePreview logo" />
</p>

# TubePreview

**Preview before you publish.**

See how your thumbnails, titles, and channel branding look in a YouTube-style
home feed on Android. No YouTube account required.

[**Download the latest APK →**](https://github.com/notrenflare/TubePreview-releases/releases/latest)

## What you can do

- Add a thumbnail and an optional channel photo.
- Customize the channel name, video title, views, and duration.
- See your video alongside sample videos and adjust its placement in the feed.
- Switch between light mode and a pure-black dark theme, including during setup.
- Reset your preview whenever you want a fresh start.

TubePreview is an independent preview tool and is not affiliated with YouTube or Google.
It does not upload videos or modify your YouTube channel.

## Screenshots

<p align="center">
  <a href="docs/screenshots/onboarding.png"><img src="docs/screenshots/onboarding.png" width="210" alt="TubePreview onboarding with a theme toggle" /></a>
  <a href="docs/screenshots/home-dark.png"><img src="docs/screenshots/home-dark.png" width="210" alt="Thumbnail preview in the dark feed" /></a>
  <a href="docs/screenshots/home-light.png"><img src="docs/screenshots/home-light.png" width="210" alt="Thumbnail preview in the light feed" /></a>
  <a href="docs/screenshots/customize.png"><img src="docs/screenshots/customize.png" width="210" alt="Channel and video customization" /></a>
</p>

## Build and run

Requires Android 7.0 (API 24) or newer.

Open the project in Android Studio with JDK 21 and Android SDK Platform 37.
After Gradle sync completes, run the `app` configuration on a device or emulator.

```sh
bash ./gradlew :app:assembleDebug
```

## Privacy and security

Imported images and preview settings stay in private app storage. There is no
sign-in or backend service, and imported images are not uploaded. Sample feed photos
load from Unsplash over HTTPS. App data is excluded from Android backup and device
transfer; uninstalling the app removes your local preview settings.

See [SECURITY.md](SECURITY.md) to report a vulnerability.

## Development

Built with Kotlin and Jetpack Compose. See [CONTRIBUTING.md](CONTRIBUTING.md)
for setup, validation, and release signing. Build and security results are available
in [GitHub Actions](https://github.com/notrenflare/TubePreview/actions).

Made with ❤️ by [**renflare**](https://github.com/notrenflare).
