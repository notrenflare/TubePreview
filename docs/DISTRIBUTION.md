# Distribution

Signed Android downloads are published through [GitHub Releases](https://github.com/notrenflare/TubePreview/releases/latest). Source is licensed under [Apache 2.0](../LICENSE).

## Build

Set `versionName` and increase `versionCode` for each application update. Preserve
the application ID `com.renflare.tubepreview` and signing certificate so users can
update without losing their saved previews.

```sh
bash ./gradlew clean :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:assembleRelease \
  --init-script scripts/release-distribution.init.gradle \
  --no-build-cache --no-configuration-cache
```

The distribution configuration retains non-debuggable release packaging and
baseline profiles. Code and resource shrinking are disabled.

## Sign

Configure `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`, using alias
`upload`. Without signing configuration, Gradle produces an unsigned APK.
Keep signing material outside Git and back it up securely.

The maintainer's macOS helper can sign with the existing Keychain identity:

```sh
node scripts/sign-release.mjs app/build/outputs/apk/release/app-release-unsigned.apk /private/tmp/TubePreview-1.1.1.apk
```

The helper checks the certificate and refuses to overwrite an existing output.

Certificate SHA-256:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

## Publish

1. Build from a committed revision and pass tests, lint, dependency checks, and secret scanning.
2. Verify the signed APK with `apksigner` and `zipalign`. Check its package, version, permissions, and release flags.
3. Test the exact APK on a device, including onboarding, imports, comparisons, refresh, both themes, and saved settings.
4. Generate checksums and complete a VirusTotal analysis for the signed APK.
5. Publish the verified APK, checksums, and certificate fingerprint. Download the public asset and confirm its checksum.
6. Update the [verification record](RELEASE_VERIFICATION.md).

Release tags and binaries are immutable. Source and build configuration remain
available for reproducibility. See [CONTRIBUTING.md](../CONTRIBUTING.md) for checks
and [SECURITY.md](../SECURITY.md) for vulnerability reports.
