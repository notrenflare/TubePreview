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

The release identity is retained in macOS Keychain. Use
[scripts/sign-release.mjs](scripts/sign-release.mjs) to sign an unsigned APK with
that existing identity. See [Distribution](docs/DISTRIBUTION.md) for commands,
backup and recovery, and the public download repository's release checklist.

Gradle also supports `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` from
the local build environment, using alias `upload`. Keep signing material outside
Git. Never put passwords in shell command arguments, committed files, or logs.

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
