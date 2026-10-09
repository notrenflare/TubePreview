# Security review

## Feed controls and metadata follow-up, 2026-10-09

The feed refresh exposes theme, notifications, and search controls without an
overflow menu, uses compact display-only metadata, pill duration badges,
horizontal context controls, a flush bottom navigation bar, and revised Shorts
artwork and tile layout. Third-party credits remain accessible from a dedicated
licenses entry in Settings. Saved preview values and image import behavior are
preserved. The change adds no permissions, dependencies, network destinations,
credential use, or exported Android components.

All 40 local unit tests pass with zero failures or skips. Debug/release builds and
both lint variants pass. A fresh OSV check at 2026-10-09T04:42:53Z found no known
advisories among 320 resolved Maven package/version pairs. Native emulator review
covers dark/light mode, the Shorts shelf, the settings footer, and a 320dp phone
width at 130% text size. Unused floating-bar backdrop code was removed. The existing
signing certificate and application ID are retained for distribution.

The owner reports that the earlier Play Protect warning disappeared after the
review submission. This is a device observation, not a claim that Google has
approved every future APK or that all devices will show identical installation
behavior. New binaries can receive independent install-time security checks.

## UI and branding follow-up, 2026-10-09

The current UI and artwork refresh adds bundled Streamline Plump vector icons and
Nunito fonts with attribution and license notices. The preview feed retains standard
sans-serif typography, full-width thumbnails, and familiar Material navigation icons.
It introduces no new permissions, network services, or credential use. All 36 local
unit tests, debug/release builds, and both lint variants pass. A fresh OSV check at
2026-10-08T21:33:04Z found no known advisories among 320 resolved Maven package/version
pairs. History-secret and independent build checks run in the private repository's
GitHub Actions workflow. The adaptive launcher's white background pixel was replaced
with an opaque red drawable matching the supplied artwork; a regression check covers
both launcher resources at 24, 48, and 96 pixels on API 26 and 36.

The distributed APK is externally signed with the existing renflare certificate;
its checksum and public signing fingerprint accompany the public 1.0 release.
Phone updates retain the application identity and local data. Google Play Protect's
new-developer warning is a separate review issue and is not claimed as resolved.

The dated original review below records the earlier publication preparation.

Review date: 2026-10-08. Scope: the local TubePreview Android application, its assets,
resolved app/test/build-tool dependencies, publication contents, and release APK.
The initial review preceded Git initialization. The local repository is now initialized
on `main`. GitHub authentication is confirmed for `notrenflare`, with private repository
visibility selected. Private source publication was authorized after documenting that
revocation of the retired API key remains unverified. The publication set excludes
the old credential and its generated outputs.

## Result

The reviewed publication set and clean release APK strings contain no secrets detected
by Gitleaks. All 320 unique resolved Maven package/version pairs returned no known
advisories from OSV at 2026-10-08T00:30:18Z. All 23 unit tests pass, debug and release
builds succeed, and both lint variants report zero errors.

After repository naming cleanup and the application ID/source namespace change to
`com.renflare.tubepreview`, the remaining 22 unit tests pass after a clean build.
Both application variants and the instrumentation test APK build; both lint variants
report zero errors and 20 dependency-update warnings. The packaged release manifest
uses the new identity for the launcher activity, startup provider authority, and
app-specific permission. Fresh publication and compiled APK string scans found no
secrets. One arithmetic template test was removed; the app-resource and splash tests
were renamed.

**Revocation of the retired API key remains unverified.**
The old .env key was embedded in generated debug/release BuildConfig and lint models.
Removing the key from the project and rebuilding removes local copies from those outputs;
it cannot revoke the credential or invalidate any earlier APKs. Key validity and prior use
were not tested.

The retired credential was present in TubePreview, but its owning Google Cloud project
has not been established. The initial scan intentionally redacted its value, and the
review archives contain no recoverable value for an exact comparison. The original app
package is no longer installed on the connected test device. The currently signed-in
Google account has a Gemini project and key, but their relationship to the retired
credential is unconfirmed. No Google Cloud key has been changed or revoked during this
investigation. The current app does not use a Gemini API key.

## Security alignment areas, priority, and implementation

| Area | Priority / impact | Concrete action |
| --- | --- | --- |
| Credential exposure | High: extracted APKs exposed an unused retired API key | Removed .env/.env.example and the Secrets Gradle plugin; disabled unused BuildConfig generation; cleaned and rebuilt app output. |
| Dependency security | High/medium: known issues in test/build tooling | Upgraded Kotlin/Compose compiler 2.2.10 → 2.4.20; constrained Bouncy Castle test and build libraries to 1.86, build-tool Commons Lang to 3.18.0, jose4j to 0.9.6, and JDOM to 2.0.6.1. No advisory suppressions were added. |
| Private data isolation | Medium: default backup included imported images/settings | Explicitly disabled backup and excluded every supported storage domain from legacy backup, cloud backup, and device transfer. |
| Image input handling | Medium: compressed byte limits alone do not bound resolution | Kept the actual-byte 2 MB limit and added a 40 MP / 16,384-pixel-per-side limit using Long multiplication before displaying imported images; invalid imports are deleted. Added resolution and deletion-boundary tests. |
| Transport security | Medium: cleartext downgrade protection | Explicitly set usesCleartextTraffic=false. Preserved default TLS certificate/hostname verification and existing HTTPS sample photos. |
| Publication safeguards | High: local credentials/build files can be accidentally uploaded | Expanded .gitignore for all module build output, IDE/generator state, dotenv files, private keys, and keystores. Reviewed a temporary Git index representing eligible first-commit files. |
| Build supply chain | Medium | Matched the wrapper JAR against the official Gradle checksum; pinned the distribution checksum. GitHub Actions use immutable revisions, read-only permissions, no persisted checkout credentials, and a checksum-verified Gitleaks binary. |
| Continued detection | Medium | Added CI for history secret scans, compilation/unit tests/lint, and resolved-dependency OSV checks; added weekly Dependabot configuration and SECURITY.md. Network/incomplete OSV scans fail the check. |
| Unused code and assets | Low: reduce unnecessary dependencies and clutter | Removed unused libraries/plugins, obsolete vector logos and template XML colors, 18 unused Kotlin color/gradient constants, unused imports, and trailing whitespace. Preserved the actual launcher/in-app logo resources and current color values. |

## Component and permissions review

The final packaged release manifest contains:
- INTERNET as its only platform uses-permission.
- AndroidX's signature-level DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION.
- MainActivity exported for MAIN/LAUNCHER, as required for launching the app.
  It does not read incoming intent extras, redirect nested intents, or process deep links.
- AndroidX InitializationProvider with exported=false.
- AndroidX ProfileInstallReceiver exported with the privileged android.permission.DUMP
  gate. Ordinary third-party apps cannot invoke its operations.
- No app-defined exported service or provider, broad storage/media permissions,
  camera/microphone/location permissions, or debuggable=true.

Application source contains no WebView/JavaScript bridge, raw SQL, external command
execution, authentication/payment endpoints, embedded credential use, trust-all TLS
implementation, or app-defined PendingIntent/Binder/broadcast interface.
Photo selection uses permission-free GetContent URI grants; imports are copied to
private files. User images are not uploaded. Sample requests go to images.unsplash.com;
that host receives ordinary network request metadata such as the device IP address.

## Scope of changes

Modified implementation/configuration files:
- app/build.gradle.kts
- build.gradle.kts
- gradle/libs.versions.toml
- .gitignore
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/renflare/tubepreview/data/ImageStorage.kt
- app/src/main/java/com/renflare/tubepreview/ui/viewmodel/CreatorViewModel.kt
- app/src/main/res/xml/backup_rules.xml
- app/src/main/res/xml/data_extraction_rules.xml
- app/src/test/java/com/renflare/tubepreview/ImageStorageTest.kt
- gradle/wrapper/gradle-wrapper.properties
- app/src/main/java/com/renflare/tubepreview/ui/components/VideoCard.kt
- app/src/main/java/com/renflare/tubepreview/ui/screens/HomeScreen.kt
- app/src/main/java/com/renflare/tubepreview/ui/screens/SettingsScreen.kt
- app/src/main/java/com/renflare/tubepreview/ui/theme/Theme.kt
- app/src/main/java/com/renflare/tubepreview/ui/theme/Color.kt
- scripts/security-audit.init.gradle
- scripts/audit-dependencies.mjs
- .github/workflows/ci.yml
- .github/dependabot.yml
- app/src/main/java/com/renflare/tubepreview/data/SettingsRepository.kt (whitespace cleanup only)
- SECURITY.md (publication guidance and reporting process)

Removed files:
- .env (credential contents intentionally excluded from this report and diff)
- .env.example (unused retired API configuration)
- app/src/main/res/values/colors.xml
- app/src/main/res/drawable/ic_launcher_background.xml
- app/src/main/res/drawable/ic_launcher_foreground.xml
- app/src/main/res/drawable/ic_splash_empty.xml
- app/src/main/res/drawable/ic_tubepreview_brand.xml

Removed unused direct dependencies: Firebase BOM, Room runtime/compiler, Retrofit,
Moshi/converter/codegen, direct OkHttp/logging-interceptor, and KSP/secrets plugins.
Coil's required transitive HTTPS networking remains. Unused commented camera,
location, permissions, and Firebase aliases were also removed.

Detailed patch evidence is retained in the local security review archive.
Credential values are excluded from the review artifacts.

## Verification evidence

| Check | Result |
| --- | --- |
| Targeted ImageStorageTest | 9 passed, including 3 new security tests |
| Full :app:testDebugUnitTest | 23 passed, 0 failed, 0 skipped |
| :app:assembleDebug / :app:assembleRelease | Both succeeded after a clean build with the patched toolchain |
| :app:lintDebug / :app:lintRelease | 0 errors; each has 20 update-availability warnings only. No unused-resource or component/security warnings remain. |
| Exact resolved Maven dependency scan | 320 unique packages, no known OSV advisories |
| Gitleaks 8.30.1 publication snapshot | No detected secrets |
| Gitleaks unpacked release resources | No detected secrets |
| Extracted DEX/resource-table strings | 16.05 MB scanned, no detected secrets; this additional pass covers strings skipped by binary-file scanning |
| Full workspace rescan | No remaining Google key findings. One generic match is an IDE planning-mode map identifier with a boolean value, not an authentication key. The entire .idea directory is excluded from publication. |
| Gradle wrapper integrity | JAR SHA-256 matches official 9.8.0 release: 238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5 |
| GitHub Actions validation | actionlint 1.7.12 passed; shellcheck/pyflakes were unavailable and disabled |
| Whitespace check | First-commit git diff --cached --check passed using the isolated index with Windows CRLF treated as line endings |

Dependency advisories fixed include:
- [Kotlin build-cache deserialization, CVE-2026-53914](https://github.com/advisories/GHSA-r937-wjx7-w2jp).
- [Commons Lang recursion, CVE-2025-48924](https://github.com/advisories/GHSA-j288-q9x7-2f5v).
- [jose4j compressed JWE denial of service, CVE-2024-29371](https://github.com/advisories/GHSA-3677-xxcr-wjqv).
- [JDOM XML external entity injection, CVE-2021-33813](https://github.com/advisories/GHSA-2363-cqg2-863c).
- [Bouncy Castle bcpkix, CVE-2026-5588](https://github.com/advisories/GHSA-wg6q-6289-32hp),
  plus GHSA-574f-3g2m-x479, GHSA-9pwp-9qqc-pr26, GHSA-c3fc-8qff-9hwx,
  and GHSA-qp49-qgx5-5m26 in bcprov.
These were found in build/test scopes; the initial runtime scan found no advisories.
The [Bouncy Castle 1.86 release](https://www.bouncycastle.org/resources/new-release-bouncy-castle-java-1-86/)
contains further security fixes, and [Kotlin 2.4.20 is a stable release](https://kotlinlang.org/docs/releases.html).

## Behavior and limitations

The app's design, branding, and normal image/preview workflows are preserved.
The application ID and source namespace are now `com.renflare.tubepreview`.
This is a new installation identity: existing test installs remain intact, but their
local data does not transfer automatically to the new app. Security changes reject
unusually large-resolution images and prevent automatic restoration of photos/settings
after uninstall or device migration. Backup behavior follows the
[Android backup configuration](https://developer.android.com/identity/data/autobackup).

This is a dated static/source, dependency, artifact, and unit-test review, not a guarantee
that the app is bulletproof. It does not include on-device fuzzing, penetration testing,
a formal proof, or analysis of every dependency's source. OSV can miss new/unreported
issues; Gitleaks does not prove that every encoded secret is absent. Android OS/media
decoder vulnerabilities depend on device patch levels.

The initial workspace had no Git repository or commit history. Publication preparation
initialized Git on `main` and verified a clean copy of the staged files independently
of ignored local IDE files: 22 unit tests, both application builds, and both lint
variants pass. A fresh OSV check at 2026-10-08T01:52:20Z found no advisories among 320
resolved Maven package/version pairs; the staged publication secret scan also passed.
The reviewed source was published through Git to the private repository
[notrenflare/TubePreview](https://github.com/notrenflare/TubePreview). Fresh onboarding,
dark/light feed, and customization screenshots use a cleared test installation.
No credentials, local IDE state, build outputs, or signing material were uploaded.

The initial GitHub Actions history scan passed. Its Android job found a vendor-specific
Gradle daemon requirement that attempted to download a JetBrains JDK despite the runner
having Temurin 21 installed. The daemon criteria now accept any locally installed JDK 21;
local unit tests, both app variants, and both lint variants pass with that configuration.
Current CI results are available in the repository's Actions tab.

Dependabot vulnerability alerts and automated security updates are enabled. Workflow
default permissions are read-only, and workflows cannot approve pull requests. GitHub
rejected private main-branch protection with an upgrade requirement and rejected hosted
secret scanning as unavailable for this repository; neither is claimed as enabled.
The CI Gitleaks history scan remains configured. Private vulnerability reporting applies
to public repositories; this private repository uses the reporting route in SECURITY.md.

Gradle produces an unsigned release APK. The distribution signing step uses external
signing material through the signing helper; keystores and private keys are excluded
from Git. Only the signed, verified APK is distributed.
Android Studio sync is still failing as it was before the audit; command-line Gradle
builds, unit tests, and lint all succeed. No device-required instrumentation tests were run.
