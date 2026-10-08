# Distribution

## Repository layout

Keep `notrenflare/TubePreview` private. Use a separate public repository,
`notrenflare/TubePreview-releases`, for the product README, screenshots, release
notes, and signed APK downloads. This is the recommended layout; the public
repository and first release have not been created yet.

Do not fork, mirror, or change the visibility of the source repository. Populate
the download repository from an explicit allowlist of release documentation and
reviewed images. Do not copy Git history, source, internal audit documents, build
reports, signing keys, or credentials.

GitHub includes source archive links on releases. In the download repository,
those archives contain only its public documentation and assets. A private
repository's release attachments require repository access.

A public APK can still be analyzed or decompiled. Keeping the source repository
private controls access to the original source and history; it cannot make client
code or embedded secrets confidential.

## First signing key

1. In Android Studio, select **Build > Generate Signed App Bundle / APK**.
2. Select **APK**, choose the `app` module, then select **Create new**.
3. Save the keystore outside this repository. Use the key alias `upload` to match
   the project's release configuration.
4. Choose strong passwords and a long validity period (at least 25 years).
   Store passwords in a password manager and keep an encrypted backup of the
   keystore in a separate location. Do not send passwords through chat.
5. Select the release variant and generate the signed APK. Record the certificate's
   SHA-256 fingerprint without including private key material.

If an existing release key has already been used for distribution, reuse it.
Losing the key can prevent direct APK users from installing future updates.
The current release build is unsigned until a release key is supplied.

## Release checklist

1. Set the app version name and increment the version code for each update.
2. Run the validation commands in [CONTRIBUTING.md](../CONTRIBUTING.md), confirm CI
   passes, and check the signed APK with Android SDK `apksigner verify --verbose
   --print-certs`.
3. Install the signed release build on a test device and verify onboarding, image
   import, preview customization, light/dark themes, and persisted settings.
4. Name the download `TubePreview-<version>.apk` and generate a SHA-256 checksum
   with `shasum -a 256`. Check the APK contents for embedded credentials and private
   test data before uploading.
5. Create a release in the public download repository with a version tag, concise
   release notes, the signed APK, and its checksum. Do not attach an AAB or debug
   APK as the direct installation download.
6. Link the public README to **Releases / Latest** and explain the minimum Android
   version and installation steps. Confirm the release and APK are accessible
   without signing in.

## Installation instructions for the public README

1. Open the latest release and download the `.apk` under **Assets**.
2. Open the downloaded file. Android may ask you to allow installation from the
   browser or file manager you used; allow it only for the intended installation.
3. Install TubePreview. You can turn that installation permission off afterward.
4. For updates, install the newer APK signed with the same key; uninstalling first
   removes local preview settings and imported images.

## Branch protection

GitHub requires an eligible paid plan to enforce branch protection on a private
personal repository. Dismissing its reminder does not enable protection. Once
eligible, protect `main` against force pushes and deletion, enforce protection for
administrators, and require the `android` and `secrets` status checks. Public
repositories can use branch protection on GitHub Free.

## References

- [GitHub protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub releases](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
