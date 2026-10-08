# Security

TubePreview previews thumbnails locally. It has no sign-in, backend API, or embedded API keys.
Sample feed photos are fetched from images.unsplash.com over HTTPS. Imported photos and
preview settings stay in private app storage and are excluded from Android backup and
device transfer. Clearing app data or uninstalling the app removes those local settings.

## Reporting a vulnerability

After publishing, enable GitHub private vulnerability reporting under repository
Settings → Security. Use the repository's Security → Report a vulnerability form.
Do not disclose keys, private photos, or exploit details in a public issue.

## Before publishing

- Revoke the retired API key previously used by this project's removed .env configuration.
  Earlier generated builds contained it; deleting the file does not revoke the key.
- Publish with Git so .gitignore excludes local settings, build output, and signing keys.
  Do not upload a ZIP of the entire workspace.
- Enable GitHub secret scanning and push protection, Dependabot alerts, and private
  vulnerability reporting where available. Require the CI checks before merging.
- Keep release signing keys outside the repository. KEYSTORE_PATH, STORE_PASSWORD,
  and KEY_PASSWORD are environment variables used only for signing. Builds without a
  keystore produce an unsigned release; never distribute a debug build as a release.
- Future authenticated services must keep privileged credentials on a server. An APK
  cannot keep a shared API secret confidential, even with obfuscation.

## Repeatable local checks

Use JDK 21, the Android SDK, Node 22 or newer, and Gitleaks 8.30.1 or newer.

```sh
bash ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease :app:auditDependencies --init-script scripts/security-audit.init.gradle --no-configuration-cache
node scripts/audit-dependencies.mjs
gitleaks git --redact=100 --log-opts="--all" .
```

The OSV check uses exact resolved Maven versions across app, unit test,
instrumentation test, and build-plugin dependencies, writes reports to build/security,
and exits unsuccessfully for known vulnerabilities or an incomplete scan.
CI scans Git history, pins action revisions and the Gitleaks download checksum,
and has read-only repository permissions. Dependabot checks for updates weekly.

No automated scan proves the absence of exploitable bugs. See SECURITY_AUDIT.md for
the dated review, evidence, and remaining limitations.
