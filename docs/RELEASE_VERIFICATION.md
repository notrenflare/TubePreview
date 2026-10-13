# Release verification

## TubePreview 1.1.1 — unminified build

Verification date: **2026-10-13 (UTC)**. This is the recommended APK in [release v1.1.1+unminified](https://github.com/notrenflare/TubePreview/releases/tag/v1.1.1%2Bunminified).

| Property | Value |
| --- | --- |
| File | `TubePreview-1.1.1.apk` |
| Package | `com.renflare.tubepreview` |
| Version | `1.1.1` (version code `5`) |
| Size | 12,834,129 bytes |
| Minimum / target Android API | 24 / 37 |
| Source commit | [`4a997c5`](https://github.com/notrenflare/TubePreview/commit/4a997c5c388bc919aea968bd01095e48797ec5b8) |
| Build configuration | Non-debuggable; code and resource shrinking disabled |

APK SHA-256:

```text
fff70281833e5f6c71845e4daaae227caa86718bd2df03b422469ee6b86f04bd
```

Signing certificate SHA-256:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

### Build and installation checks

- Retains all current app features, the existing package name, release certificate, and baseline profiles.
- Unit tests, debug/release lint, and full release assembly passed with the published unminified configuration. Rebuilt code, resources, profiles, and source metadata matched the reviewed APK.
- Android SDK `apksigner` verified v2/v3 signatures and the established certificate. ZIP integrity and 16 KB alignment checks passed.
- The manifest is non-debuggable, disables backup and cleartext traffic, and requests `INTERNET` as its only platform permission. AndroidX also declares an internal signature-level receiver permission.
- A normal update on a Samsung SM-A556E completed without a Play Protect warning at **18:53 UTC** with protections enabled. The installed APK matched the checksum above, version code `5` was confirmed, and the app launched successfully.
- The published build configuration is provided in `release-unminified.init.gradle`, included with the release and maintained under `scripts/`.

### Security vendor analysis

[VirusTotal report for this APK](https://www.virustotal.com/gui/file/fff70281833e5f6c71845e4daaae227caa86718bd2df03b422469ee6b86f04bd)

The completed analysis at **2026-10-13 14:30:53 UTC** showed **0 detections across 67 engines**, including Google Undetected. Eight additional engines could not process the file type; those results are not counted as clean verdicts.

A Play Protect review for this exact package and APK hash was submitted on **2026-10-13**. The later normal-install result above applies to this exact binary. It is a dated device observation, not a guarantee of platform approval or the absence of future warnings. The earlier optimized APK and its immutable release remain preserved.

## TubePreview 1.1.1 — optimized build

Verification date: **2026-10-13**. These results apply to the signed APK attached to [release v1.1.1](https://github.com/notrenflare/TubePreview/releases/tag/v1.1.1).

| Property | Value |
| --- | --- |
| File | `TubePreview-1.1.1.apk` |
| Package | `com.renflare.tubepreview` |
| Version | `1.1.1` (version code `5`) |
| Size | 2,132,972 bytes |
| Minimum / target Android API | 24 / 37 |
| Source commit | [`8a4363e`](https://github.com/notrenflare/TubePreview/commit/8a4363e333c49f413bdbb8149b9261090bbfd9e7) |
| Source tag | [`v1.1.1`](https://github.com/notrenflare/TubePreview/tree/v1.1.1) |

APK SHA-256:

```text
b09fcc95868fd2d73cc2af26ea02e3c175b7565433cff8ff11fb619f08eb7ca6
```

Signing certificate SHA-256:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

### Build and binary checks

- A clean build with build and configuration caches disabled passed all **127 unit tests**, debug/release lint, and release assembly.
- [GitHub CI](https://github.com/notrenflare/TubePreview/actions/runs/38395939760) passed build, lint, tests, full-history secret scanning, and dependency advisory checks. The local resolved-dependency check covered **320 packages**, with no known OSV findings.
- Android SDK `apksigner` verified APK v2/v3 signatures and the established certificate. `zipalign` and ZIP integrity checks passed.
- Packaged source metadata identifies the source commit above. An anonymous download from GitHub matched the local APK SHA-256 and signing certificate.
- The packaged manifest is non-debuggable, disables backup and cleartext traffic, and requests `INTERNET` as its only platform permission. AndroidX also declares an internal signature-level receiver permission.
- The exact signed APK installed and launched on a Pixel 10a emulator. Home feed and settings rendering were checked.

Release builds use R8 code optimization and resource shrinking. Compared with the unminified 1.0 APK, compressed DEX content decreased from 11,700,030 to 1,401,136 bytes, accounting for approximately 97% of the APK size reduction. This release changes packaging configuration and version metadata; application features remain unchanged.

### Security vendor analysis

[VirusTotal report for this APK](https://www.virustotal.com/gui/file/b09fcc95868fd2d73cc2af26ea02e3c175b7565433cff8ff11fb619f08eb7ca6)

Completed analyses at **2026-10-13 12:20:05 UTC** and **2026-10-13 12:57:57 UTC** both showed **0 detections across 68 engines**. Seven additional engines could not process the file type; those results are not counted as clean verdicts.

A normal update attempt on a Samsung SM-A556E displayed Google's **Uncommon** warning: “Play Protect hasn't seen an app from this developer before. It may be unsafe.” Installation was blocked; the existing 1.0 installation was retained.

A Play Protect review for this exact package and APK hash was submitted on **2026-10-13**. A supplemental review included prior case references and controlled installation results. Google does not send appeal decision responses; the outcome is checked through ordinary installation with Play Protect enabled.

The optimized APK still displayed the Uncommon warning during a normal Samsung update at **18:44 UTC** on 2026-10-13. The separately reviewed unminified build passed normal installation later that day and is now the recommended download. The optimized binary, source tag, and signing identity remain unchanged.

## TubePreview 1.1.0

Verification date: **2026-10-13**. These results apply to the signed APK attached to [release v1.1.0](https://github.com/notrenflare/TubePreview/releases/tag/v1.1.0).

| Property | Value |
| --- | --- |
| File | `TubePreview-1.1.0.apk` |
| Package | `com.renflare.tubepreview` |
| Version | `1.1.0` (version code `4`) |
| Size | 2,132,972 bytes |
| Minimum / target Android API | 24 / 37 |
| Source tag | [`v1.1.0`](https://github.com/notrenflare/TubePreview/tree/v1.1.0) |

APK SHA-256:

```text
14d570e7764e6dad3188144bde5ebb9380f1a517306c434c7acf5382e0171f64
```

Signing certificate SHA-256:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

### Checks

- `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleRelease` passed.
- Android SDK `apksigner` verified the APK signatures and release certificate; the APK installed on a Pixel 10a emulator.
- The packaged manifest remains non-debuggable, disables backup and cleartext traffic, and requests `INTERNET` as its only platform permission. AndroidX also declares an internal signature-level receiver permission.

### Security vendor analysis

[VirusTotal report for this APK](https://www.virustotal.com/gui/file/14d570e7764e6dad3188144bde5ebb9380f1a517306c434c7acf5382e0171f64)

At **2026-10-13 11:12:45 UTC**, the report showed **0 detections**. A repeat upload and analysis completed at **2026-10-13 11:48:01 UTC**, with **0 detections across 66 engines**. Seven additional engines could not process the file type, one timed out, and one failed; these are not clean verdicts.

Play Protect reviews for this package and APK hash were submitted on **2026-10-13**, with acknowledgment from Google's form. A Samsung SM-A556E still displayed the Uncommon warning during ordinary installation. Submission is not platform clearance or a guarantee against future warnings.

These are dated observations for this exact binary, not a guarantee of safety or platform approval. Vendor results can change, and Android installation prompts are determined separately by Google Play Protect. Follow Android's security checks rather than disabling them.

## TubePreview 1.0

Verification date: **2026-10-11**. These results apply to the signed APK attached to [release v1.0](https://github.com/notrenflare/TubePreview/releases/tag/v1.0).

| Property | Value |
| --- | --- |
| File | `TubePreview-1.0.apk` |
| Package | `com.renflare.tubepreview` |
| Version | `1.0` (version code `3`) |
| Size | 12,768,593 bytes |
| Minimum / target Android API | 24 / 37 |
| Source commit | [`90a3cfb`](https://github.com/notrenflare/TubePreview/tree/90a3cfbf7bebe9073bf32e623a8ddec4f7cf91c8) |

APK SHA-256:

```text
1e746f14a4f336e2ea1c0dc41a4d514fc8f19f878f2aae4e88634cea6086c5d7
```

Signing certificate SHA-256:

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

### Checks

- Android SDK `apksigner` verified the APK signatures and release certificate.
- GitHub release attestation and asset verification matched the APK checksum.
- The packaged manifest is non-debuggable, disables backup and cleartext traffic, and requests `INTERNET` as its only platform permission. AndroidX also declares an internal signature-level receiver permission.
- The repository-history secret scan found no detected secrets.
- The resolved dependency advisory check covered 320 unique Maven package/version pairs and returned no known OSV findings.

### Security vendor analysis

[VirusTotal report for this APK](https://www.virustotal.com/gui/file/1e746f14a4f336e2ea1c0dc41a4d514fc8f19f878f2aae4e88634cea6086c5d7)

At **2026-10-11 01:27:13 UTC**, the report showed **0 detections across 67 engines**. Eight additional engines could not process the file type; their results are not counted as clean verdicts.

These are dated observations for this exact binary, not a guarantee of safety or platform approval. Vendor results can change, and Android installation prompts are determined separately by Google Play Protect. Follow Android's security checks rather than disabling them.
