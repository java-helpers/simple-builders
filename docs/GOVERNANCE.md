# Governance & Maintenance

This document describes how Simple Builders is maintained and what adopters —
especially those with high-security / high-reliability requirements — can rely
on and plan for.

## Maintainers

Simple Builders is currently maintained by a single maintainer (see the
`<developers>` section of the root [`pom.xml`](../pom.xml)).

We explicitly acknowledge the resulting **bus factor of one**: the project does
not currently have a second maintainer who can guarantee continuity if the
primary maintainer becomes unavailable. The policies below exist so adopters can
make an informed risk assessment and remain self-reliant regardless of
maintainer availability.

We welcome additional maintainers. If you would like to help maintain the
project, please start by contributing (see [CONTRIBUTING.md](CONTRIBUTING.md))
and then open an issue expressing interest.

## Maintenance guarantees

Maintenance is provided on a **best-effort** basis:

- **Security fixes** follow [`SECURITY.md`](../SECURITY.md): supported versions
  are listed there, vulnerabilities are handled via private reporting, and fixes
  are shipped as patch releases for the latest minor version.
- **Dependencies** are kept current via Dependabot (see
  [`.github/dependabot.yml`](../.github/dependabot.yml)) and enforced on pull
  requests by Dependency Review.
- **Releases** are cut manually (see [`RELEASE.md`](../RELEASE.md)); there is no
  fixed release cadence.

No warranty is provided beyond the terms of the [MIT License](../LICENSE).

## Supply-chain self-reliance

Each release is designed so adopters can verify and, if necessary, reproduce it
without depending on the maintainer being reachable:

- **GPG-signed** artifacts published to Maven Central.
- **SBOM** (CycloneDX) generated per module.
- **Build provenance** attestations for published jars.
- **Reproducible** archives (`project.build.outputTimestamp`).

These let you pin, verify, mirror, and audit exactly what you consume.

## Vendoring / fork strategy for adopters

Because the project is MIT-licensed, adopters are free to vendor or fork it. For
high-reliability use we recommend:

1. **Pin exact versions** (never version ranges) and record the artifact
   checksums / verify the GPG signatures.
2. **Mirror the artifacts** you depend on in an internal repository (e.g.
   Artifactory/Nexus) so your builds do not depend on upstream availability.
3. **Keep a fork.** If upstream maintenance stalls and you need a fix, fork the
   repository, apply the change on a branch, and build/publish under your own
   coordinates (or a `-internal` classifier). Simple Builders is a
   compile-time-only annotation processor with no runtime dependency on this
   project, which keeps such forks low-risk.
4. **Watch releases and advisories** for the upstream repository so you learn
   about security fixes even if you build from a fork.

## Reporting problems

- Security vulnerabilities: see [`SECURITY.md`](../SECURITY.md) (private
  reporting — do **not** open a public issue).
- Bugs and feature requests: the
  [issue tracker](https://github.com/java-helpers/simple-builders/issues).
