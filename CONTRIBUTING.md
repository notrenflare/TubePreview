# Contributing

## Local setup

Use Android Studio, JDK 21, Android SDK Platform 37, and Node.js 22 or newer.
Clone the repository with an account that has access and open the root directory.
Run the `app` configuration on Android 7.0 (API 24) or newer.

The app does not require API keys or a backend. The application ID is
`com.renflare.tubepreview`; the Gradle wrapper manages the Gradle version.

## Making a change

1. Create a branch with a short descriptive name, such as `fix/image-import`.
2. Keep the change focused and follow the existing Kotlin and Compose patterns.
3. Run the checks below and review `git diff --check` before committing.
4. Open a pull request describing the problem, resulting behavior, and validation.
   Include screenshots when the UI changes, using sample content only.
5. Wait for the build and security checks to pass before merging.

```sh
bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease :app:auditDependencies --init-script scripts/security-audit.init.gradle --no-configuration-cache
node scripts/audit-dependencies.mjs
gitleaks git --redact=100 --log-opts="--all" .
git diff --check
```

Device tests live in `app/src/androidTest/` and run separately on an emulator or
connected device. CI runs local unit tests, builds, lint, dependency checks, and
Git history secret scans.

## Release signing and distribution

Supply `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` through the local
build environment. The signing key alias is `upload`. Keep the keystore outside
this repository and retain an encrypted backup; app updates must use the same
signing identity. Never put passwords in commands, committed files, or logs.

Without a release keystore, `assembleRelease` produces an unsigned APK that must
not be published as an installable release. See [Distribution](docs/DISTRIBUTION.md)
for the public download repository and release checklist.

## Project layout

| Location | Purpose |
| --- | --- |
| `app/src/main/java/com/renflare/tubepreview/data/` | Settings, image storage, and sample feed data |
| `app/src/main/java/com/renflare/tubepreview/ui/` | Compose screens, components, themes, and ViewModel |
| `app/src/main/res/` | Android resources and app branding |
| `app/src/test/` | Local unit tests |
| `app/src/androidTest/` | Device UI tests |
| `scripts/` | Dependency inventory and security checks |
| `docs/screenshots/` | README screenshots |

## Reporting a problem

For bugs, include the Android version, device model, reproduction steps, and what
you expected to happen. Remove private content from screenshots and logs.
For security issues, follow [SECURITY.md](SECURITY.md).

Never commit credentials, signing keys, private images, or generated build output.
