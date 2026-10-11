# Distribution

Source, issues, and signed Android releases live in [notrenflare/TubePreview](https://github.com/notrenflare/TubePreview). The source is licensed under [Apache 2.0](../LICENSE).

## Build and sign

Always increase `versionCode` for each release and set `versionName` to the public release version. Keep the application ID `com.renflare.tubepreview` and the release signing identity so installed users can update without losing data.

```sh
bash ./gradlew :app:testDebugUnitTest :app:assembleRelease :app:lintRelease
node scripts/sign-release.mjs app/build/outputs/apk/release/app-release-unsigned.apk /private/tmp/TubePreview-1.0.apk
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

1. Run unit tests, debug/release builds, lint, resolved-dependency advisory checks, and complete Git-history secret scans.
2. Verify the signed APK with Android SDK `apksigner verify --verbose --print-certs`. Check package name, version, minimum SDK, permissions, and non-debuggable status.
3. Install the exact signed binary on a test device. Check onboarding, image import, avatar framing, comparison cycling, theme switching, refresh, and persisted settings.
4. Generate `SHA256SUMS.txt` using `shasum -a 256`. Include `SIGNING_CERTIFICATE.txt` and release notes linking the source commit.
5. Assemble a draft release with all assets, then publish. Verify that an anonymous download matches the local checksum and certificate.

Published APKs and release tags are preserved. Releases are published in this repository. Documentation may be corrected without replacing a binary.

A scan or earlier provider review is evidence only for that exact binary. Android installation warnings are controlled by the platform; do not claim universal clearance or recommend disabling Play Protect.

## Repository checks

GitHub Actions run build and security checks with read-only permissions and pinned action revisions. Dependabot checks dependency updates weekly. Enable private vulnerability reporting and protect the default branch against force pushes and deletion when supported.

See [CONTRIBUTING.md](../CONTRIBUTING.md) for validation commands and [SECURITY.md](../SECURITY.md) for vulnerability reports.
