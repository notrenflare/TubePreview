# Release verification

## TubePreview 1.1.1

[Download](https://github.com/notrenflare/TubePreview/releases/latest) · [VirusTotal report](https://www.virustotal.com/gui/file/fff70281833e5f6c71845e4daaae227caa86718bd2df03b422469ee6b86f04bd/detection)

Verified on 2026-10-13 (UTC).

| Property | Value |
| --- | --- |
| File | `TubePreview-1.1.1.apk` |
| Package | `com.renflare.tubepreview` |
| Version | `1.1.1` (version code `5`) |
| Size | 12,834,129 bytes |
| Minimum / target Android API | 24 / 37 |
| Packaged source commit | [`4a997c5`](https://github.com/notrenflare/TubePreview/commit/4a997c5c388bc919aea968bd01095e48797ec5b8) |

### Checks

- Unit tests, debug and release lint, and release assembly passed.
- APK v2/v3 signatures, ZIP integrity, and 16 KB alignment passed.
- The release is non-debuggable, with backup and cleartext traffic disabled.
- Installation and launch were verified on a Samsung SM-A556E.
- VirusTotal reported 0 detections across 67 engines; eight additional engines could not process the file type. Results are dated observations for this exact APK.

### APK SHA-256

```text
fff70281833e5f6c71845e4daaae227caa86718bd2df03b422469ee6b86f04bd
```

### Signing certificate SHA-256

```text
e18661c71f6047130b297184bdd4d0b4497d372f0570404e89b179a1189774f3
```

The release includes checksum and signing certificate files for download verification.
