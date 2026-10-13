# Distribution

Source, issues, and signed Android releases live in [notrenflare/TubePreview](https://github.com/notrenflare/TubePreview). The source is licensed under [Apache 2.0](../LICENSE).

## Build and sign

Always increase `versionCode` for each release and set `versionName` to the public release version. Keep the application ID `com.renflare.tubepreview` and the release signing identity so installed users can update without losing data.

```sh
bash ./gradlew clean :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:assembleRelease --no-build-cache --no-configuration-cache
node scripts/sign-release.mjs app/build/outputs/apk/release/app-release-unsigned.apk /private/tmp/TubePreview-1.1.1.apk
```

The maintainer helper reads the existing encrypted identity from macOS Keychain, verifies its certificate, signs the APK, and removes temporary key material. It never generates or rotates keys. It refuses to overwrite an existing output.

Other environments can configure `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`, using alias `upload`. Without signing configuration, Gradle produces an unsigned release APK. Never publish a debug build or commit signing material.

## Release certificate

The established certificate SHA-256 fingerprint is:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

The signing identity must be backed up securely outside Git. Losing it can prevent existing direct-APK users from installing updates.

## Verification and publication

1. Commit the release version before building so packaged source metadata identifies the release commit. Run a clean build, unit tests, debug/release lint, resolved-dependency advisory checks, and complete Git-history secret scans.
2. Verify the signed APK with Android SDK `apksigner verify --verbose --print-certs` and `zipalign`. Check package name, version, minimum SDK, permissions, and non-debuggable status.
3. Install the exact signed binary on a test device. Check onboarding, image import, avatar framing, comparison cycling, theme switching, refresh, and persisted settings. Record any ordinary installation warning.
4. Generate a SHA-256 checksum and include the signing certificate fingerprint. Upload this exact APK to VirusTotal and record completed results; engines that cannot process the file are not clean verdicts.
5. Wait for CI, tag the verified source commit, and publish the release with the signed APK and verification files. Confirm that an anonymous download matches the local checksum and certificate.
6. If Play Protect flags the APK, submit its package name, exact VirusTotal hash, and public download link through Google's review form. Record submission acknowledgment separately from any review decision or device outcome.
7. Update the dated release verification record. Repeat binary checks and vendor review for each new APK; results do not transfer between hashes.

Published APKs and release tags are preserved. Releases are published in this repository. Documentation may be corrected without replacing a binary.

A scan or earlier provider review is evidence only for that exact binary. Android installation warnings are controlled by the platform; do not claim universal clearance or recommend disabling Play Protect.

## Repository checks

GitHub Actions run build and security checks with read-only permissions and pinned action revisions. Dependabot checks dependency updates weekly. Enable private vulnerability reporting and protect the default branch against force pushes and deletion when supported.

See [CONTRIBUTING.md](../CONTRIBUTING.md) for validation commands and [SECURITY.md](../SECURITY.md) for vulnerability reports.
