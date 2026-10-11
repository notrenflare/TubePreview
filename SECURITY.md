# Security policy

## Supported versions

Security fixes are applied to the latest release. Update before reporting an issue that may already be resolved.

## Reporting a vulnerability

Use [GitHub private vulnerability reporting](https://github.com/notrenflare/TubePreview/security/advisories/new) to report a suspected vulnerability. Include affected version, reproduction steps, impact, and a minimal example where possible.

Do not post credentials, private images, or exploit details in public issues. Reports are reviewed by the maintainer; no response-time guarantee is offered.

## Security model

TubePreview has no sign-in, backend API, or embedded API keys. Imported images and preview settings remain in private app storage and are excluded from Android backup and device transfer.

- Photo selection uses Android's system picker without broad storage permissions.
- Imports are bounded by actual stream size and decoded dimensions.
- Remote sample photos load from `images.unsplash.com` over HTTPS with standard TLS validation.
- Release signing material is kept outside Git. Published APKs include a checksum and certificate fingerprint.
- CI runs unit tests, lint, dependency advisory checks, and a redacted Git-history secret scan.

Automated checks reduce risk; they do not prove the absence of vulnerabilities. See the dated [release verification record](docs/RELEASE_VERIFICATION.md), [privacy](docs/PRIVACY.md), and [distribution](docs/DISTRIBUTION.md) for further details.
