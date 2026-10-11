# Contributing

Thank you for helping improve TubePreview. Focused bug fixes, accessibility improvements, and clear reports are welcome.

## Setup

Install Android Studio, JDK 21, and Android SDK Platform 37. Clone the repository and open its root directory in Android Studio. Run the `app` configuration on Android 7.0 (API 24) or newer.

The app requires no API keys or backend. Debug builds use Android's standard development keystore. Release builds remain unsigned unless signing environment variables are explicitly provided.

## Changes

1. Open an issue for a substantial feature or architectural change.
2. Create a descriptive branch, such as `fix/image-import`.
3. Follow the existing Kotlin and Compose conventions. Keep UI state in the appropriate component and persisted settings in the repository.
4. Add tests for behavior changes and run the checks below.
5. Open a pull request explaining the problem, resulting behavior, and validation. Include light/dark screenshots for UI changes, using sample content only.

Do not commit private photos, credentials, signing keys, IDE state, or generated build output.

## Checks

```sh
bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease
git diff --check
```

CI also scans all Git history for secrets and checks exact resolved dependency versions against OSV. To run those checks locally, install Node.js 22 or newer and Gitleaks 8.30.1 or newer:

```sh
bash ./gradlew :app:auditDependencies --init-script scripts/security-audit.init.gradle --no-configuration-cache
node scripts/audit-dependencies.mjs
gitleaks git --redact=100 --log-opts="--all" .
```

Unit and Compose UI tests run with Robolectric. The device test under `app/src/androidTest/` runs separately on a connected emulator or device.

## Project layout

| Location | Purpose |
| --- | --- |
| `app/src/main/java/com/renflare/tubepreview/data/` | Settings, image imports, crop state, comparison options, and sample feed composition |
| `app/src/main/java/com/renflare/tubepreview/ui/screens/` | Onboarding, home feed, and settings |
| `app/src/main/java/com/renflare/tubepreview/ui/components/` | Reusable UI, dialogs, image framing, and feed rendering |
| `app/src/main/java/com/renflare/tubepreview/ui/viewmodel/` | State flows and user actions |
| `app/src/test/` | Local unit and UI tests |
| `scripts/` | Security checks and maintainer release signing |
| `docs/` | Architecture, privacy, distribution, and screenshots |

See [architecture](docs/ARCHITECTURE.md) for data flow and [distribution](docs/DISTRIBUTION.md) for signing and release verification.

For bugs, include device model, Android version, steps to reproduce, and expected behavior. Remove private content from screenshots and logs. Use [SECURITY.md](SECURITY.md) for vulnerabilities.

## License

By submitting a contribution, you agree to license it under the project's [Apache License 2.0](LICENSE). Retain existing attribution and include the license and origin of any new third-party asset or dependency.
