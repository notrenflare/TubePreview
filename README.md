<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="96" alt="TubePreview logo" />
</p>

# TubePreview

**Preview before you publish.**

See how your thumbnails, titles, and channel branding look in a YouTube-style
home feed on Android. No YouTube account required.

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
