<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" alt="TubePreview logo" />
</p>

# TubePreview

**Preview before you publish.**

I built TubePreview to check how a thumbnail looks in a YouTube-style feed before
publishing. Add your image, try a title, and see it alongside sample videos.
No YouTube sign-in needed.

[**Download the latest APK →**](https://github.com/notrenflare/TubePreview-releases/releases/latest)

## What you can do

- See your thumbnail alongside sample videos and choose where it appears.
- Try different titles, channel photos, names, view counts, and durations.
- Compare your preview in light mode and a pure-black dark theme.

TubePreview is an independent preview tool. It isn't affiliated with YouTube or
Google, and it doesn't upload videos or change your channel.

## Screenshots

<p align="center">
  <a href="docs/screenshots/onboarding.png"><img src="docs/screenshots/onboarding.png" width="210" alt="TubePreview onboarding with a theme toggle" /></a>
  <a href="docs/screenshots/home-dark.png"><img src="docs/screenshots/home-dark.png" width="210" alt="Thumbnail preview in the dark feed" /></a>
  <a href="docs/screenshots/home-light.png"><img src="docs/screenshots/home-light.png" width="210" alt="Thumbnail preview in the light feed" /></a>
</p>
<p align="center">
  <a href="docs/screenshots/shorts.png"><img src="docs/screenshots/shorts.png" width="210" alt="Two-column Shorts preview" /></a>
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

Your thumbnails, channel photo, and preview settings stay on your device;
TubePreview doesn't upload them. Sample feed photos load from Unsplash over HTTPS.
App data isn't included in Android backup or device transfer. Uninstalling the app
removes your saved previews.

See [SECURITY.md](SECURITY.md) to report a vulnerability.

## Development

Built with Kotlin and Jetpack Compose. See [CONTRIBUTING.md](CONTRIBUTING.md)
for setup, validation, and release signing. Build and security results are available
in [GitHub Actions](https://github.com/notrenflare/TubePreview/actions).

Asset credits and licenses are in [third-party notices](THIRD_PARTY_NOTICES.md).

Made with ❤️ by [**renflare**](https://github.com/notrenflare).
