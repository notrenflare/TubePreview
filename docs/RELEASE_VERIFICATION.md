# Release verification

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

## Checks

- Android SDK `apksigner` verified the APK signatures and release certificate.
- GitHub release attestation and asset verification matched the APK checksum.
- The packaged manifest is non-debuggable, disables backup and cleartext traffic, and requests `INTERNET` as its only platform permission. AndroidX also declares an internal signature-level receiver permission.
- The repository-history secret scan found no detected secrets.
- The resolved dependency advisory check covered 320 unique Maven package/version pairs and returned no known OSV findings.

## Security vendor analysis

[VirusTotal report for this APK](https://www.virustotal.com/gui/file/1e746f14a4f336e2ea1c0dc41a4d514fc8f19f878f2aae4e88634cea6086c5d7)

At **2026-10-11 01:27:13 UTC**, the report showed **0 detections across 67 engines**. Eight additional engines could not process the file type; their results are not counted as clean verdicts.

These are dated observations for this exact binary, not a guarantee of safety or platform approval. Vendor results can change, and Android installation prompts are determined separately by Google Play Protect. Follow Android's security checks rather than disabling them.
