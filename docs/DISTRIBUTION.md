# Distribution

## Repositories

- **Private source:** [notrenflare/TubePreview](https://github.com/notrenflare/TubePreview)
- **Public downloads:** [notrenflare/TubePreview-releases](https://github.com/notrenflare/TubePreview-releases)
- **Latest APK:** [Releases / Latest](https://github.com/notrenflare/TubePreview-releases/releases/latest)

The public repository contains its product README, app logo, and five reviewed
screenshots. Releases contain the signed APK, its SHA-256 checksum, and the public
signing certificate fingerprint. Do not fork or mirror the source repository,
copy its Git history, or upload internal audit documents, build reports, or keys.

GitHub's automatic release archives contain only the public repository's
documentation and assets. A public APK can still be analyzed or decompiled;
repository privacy does not make embedded client code or secrets confidential.

## Signing identity

TubePreview 1.0 uses an RSA-3072 release signing key with alias `upload`.
The complete encrypted PKCS12 keystore and its password are stored in macOS
Keychain, outside the project. The main service name is
`com.renflare.tubepreview.release-signing`, with account `renflare`.
Additional Keychain items with the same service prefix retain the keystore parts.

The release certificate SHA-256 fingerprint is:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

[sign-release.mjs](../scripts/sign-release.mjs) restores this existing identity
into a temporary directory, signs the APK, verifies the certificate, then removes
the temporary keystore. Passwords are passed through the child process environment
and are never printed. The helper does not create or rotate signing keys.

## Build and sign

Run the validation commands in [CONTRIBUTING.md](../CONTRIBUTING.md). Set the
version name and increment the version code before each subsequent release.

Without signing environment variables, Gradle creates an unsigned release APK.
On the Mac holding the signing identity:

```sh
bash ./gradlew :app:testDebugUnitTest :app:assembleRelease :app:lintRelease
node scripts/sign-release.mjs app/build/outputs/apk/release/app-release-unsigned.apk /private/tmp/TubePreview-1.1.apk
```

Use a new output path. The helper refuses to overwrite an existing file.
Set `JAVA_HOME`, `ANDROID_HOME`, or `APKSIGNER_PATH` if tooling is installed
elsewhere.

Alternatively, supply an exported keystore with `KEYSTORE_PATH`,
`STORE_PASSWORD`, and `KEY_PASSWORD` in the local build environment. Never
put passwords in committed files, shell command arguments, or logs.

## Backup and recovery

An off-machine backup remains the owner's responsibility. The local Keychain
record is not proof that a separate backup exists.

Export the encrypted keystore to a new file:

```sh
node scripts/sign-release.mjs --export-keystore /private/tmp/TubePreview-release-backup.p12
```

Move the export to secure storage on another device or an encrypted backup.
In Keychain Access, find **TubePreview release signing identity** for the service
above. Its protected value is a JSON record; retain the `password` field in a
password manager separately from the keystore backup. Do not send either through
chat or upload them to GitHub. Keep all of the Keychain keystore-part entries.

The exported PKCS12 keystore and password can be used with Gradle's signing
environment variables on another machine. Verify the resulting APK against the
fingerprint above before publication. Losing the signing identity can prevent
existing direct-APK users from installing updates.

## Release checklist

1. Confirm unit tests, builds, lint, dependency checks, and Git history secret
   scans pass. Verify the APK with Android SDK `apksigner verify --verbose --print-certs`.
2. Install the signed release on an isolated test device. Verify onboarding,
   image import, preview customization, themes, and persisted settings.
3. Name the download `TubePreview-<version>.apk`. Generate a SHA-256 checksum with
   `shasum -a 256` and inspect the compiled manifest and packaged content for
   credentials or private test data.
4. Create a versioned release in the public download repository with the APK,
   `SHA256SUMS.txt`, and `SIGNING_CERTIFICATE.txt`. Attach neither source files
   nor debug builds.
5. Confirm the latest-release link, screenshots, and APK download work without
   GitHub authentication.

## APK review and release identity

The published 1.0 APK is frozen while its Google Play Protect review is pending.
Do not rebuild, re-sign, replace its release assets, or move its tag. Its SHA-256 is:

```text
b5c61ac33e3c787073b254a658a927343db7f9f4dccc7b6593fc9afbd5ca3304
```

On 2026-10-09, an anonymous GitHub download matched this hash and the established
release certificate. [VirusTotal](https://www.virustotal.com/gui/file/b5c61ac33e3c787073b254a658a927343db7f9f4dccc7b6593fc9afbd5ca3304/detection)
reported 0 detections among 67 scanning vendors; eight additional vendors could
not process the file type. A Google Play Protect appeal for this exact APK was
submitted and its receipt confirmed. Submission is not approval, and a scan with
no detections does not establish that an APK is safe.

For every future app change:

1. Increment the version code and use a distinct version name and release tag.
   Preserve the package name and signing identity so existing users can update.
2. Complete validation and signing before scanning the final APK. Record the hash,
   source commit, certificate fingerprint, scan date, and review status privately.
3. If Google displays a warning, review the exact affected APK through its official
   process. Never reuse an older binary's scan or appeal as evidence for a new one.
4. Assemble a draft GitHub release with all assets, then publish it once complete.
   Verify that an anonymous download matches the reviewed hash.
5. Leave published APKs and tags unchanged. Documentation and release notes may be
   corrected without rebuilding the app.

Google controls Play Protect classification. Stable signing and versioned
releases do not guarantee exemption from warnings. Developer identity verification
and Play Store publication are separate account processes involving the owner's
identity, legal agreements, and potentially fees; they are not malware-review
approvals. Do not change the signing key or package name to evade classification,
or recommend disabling Play Protect.

## Repository protection

The public download repository protects `main` against force pushes and deletion,
including for administrators. GitHub secret scanning and push protection are
enabled. Release immutability is enabled for future releases. It does not
retroactively lock the existing 1.0 release; the freeze above is an operational
rule for that APK.

The private source repository's branch protection is unavailable on the current
account plan. The reminder was dismissed; that does not enable enforcement.
After an eligible paid upgrade, enforce protection for administrators and require
the `android` and `secrets` checks on `main`.

## References

- [GitHub protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub releases](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
- [Google Play Protect warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance)
- [Android developer verification](https://developer.android.com/developer-verification/guides)
- [GitHub release immutability](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/establish-provenance-and-integrity/prevent-release-changes)
