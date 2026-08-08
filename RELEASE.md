# Release Process

This document describes how to release artifacts to Maven Central.

## Prerequisites

Configure these GitHub secrets in **Settings** → **Secrets and variables** → **Actions**:

1. **`CENTRAL_TOKEN_USERNAME`** - Maven Central token username
2. **`CENTRAL_TOKEN_PASSWORD`** - Maven Central token password
3. **`GPG_SIGNING_KEY`** - GPG private key (full armored block)
4. **`GPG_SIGNING_KEY_PASSWORD`** - GPG key passphrase

## How to Release

**Actions** → **Release to Maven Central** → **Run workflow**

No version input is required. The workflow derives the release version from the current POM by stripping the `-SNAPSHOT` suffix (e.g., `0.5.0-SNAPSHOT` → release `0.5.0`). The `main` branch must always be on a `-SNAPSHOT` version.

### What the Workflow Does

1. Derives the release version from the current POM snapshot version (strips `-SNAPSHOT`)
2. Updates POM versions to the release version in **all** modules (core, processor, example, example-custom-generator)
3. Commits the version bump on a dedicated `release/vX.Y.Z` branch and creates tag `vX.Y.Z`
4. Builds and verifies the project with `-Prelease` (reproducible builds via `project.build.outputTimestamp`)
5. Signs artifacts with GPG
6. Generates a CycloneDX **SBOM** (JSON + XML) for each published module
7. Runs tests and **stages** the deployment to the Sonatype Central portal (does **not** auto-publish)
8. Creates a **build-provenance attestation** for the published jars
9. Bumps all module versions to the next snapshot (e.g., `0.6.0-SNAPSHOT` after releasing `0.5.0`) and commits it as a second commit on the release branch
10. Pushes the release branch and tag (never pushes directly to `main`)
11. Opens a **pull request** against `main` containing both the release version commit and the next-snapshot bump
12. Creates **draft** GitHub release (jars, sources, javadoc **and SBOMs** attached; requires manual publish)

## After Release

1. **Publish to Maven Central**: Go to the [Sonatype Central portal](https://central.sonatype.com/publishing/deployments), review the staged deployment, and **manually publish** it. Nothing is released to consumers until this step is performed.
2. **Publish GitHub Release**: Go to **Releases** → Edit draft → **Publish release**
3. **Verify Maven Central**: Artifacts appear at https://central.sonatype.com/ (15-30 min delay)
   - Search for: `io.github.java-helpers:simple-builders-core` or `simple-builders-processor`
4. **Test the release**:
   ```xml
   <dependency>
     <groupId>io.github.java-helpers</groupId>
     <artifactId>simple-builders-core</artifactId>
     <version>0.5.0</version>
   </dependency>
   ```
5. **Merge the version-bump pull request**: Review and merge the PR opened by the workflow. This updates `main` with both the release version commit (tagged `vX.Y.Z`) and the next-snapshot bump (e.g., `0.6.0-SNAPSHOT`), so development can continue.

## Local Release (Advanced)

For manual local releases without GitHub Actions:

```bash
# Stage artifacts (requires manual publishing in Sonatype portal)
mvn clean deploy -Prelease

# Stage and auto-publish
mvn clean deploy -Prelease -Dcentral.autoPublish=true
```

**Note:** You need configured GPG keys and Maven Central credentials in `~/.m2/settings.xml`

## Troubleshooting

- **GPG errors**: Verify `GPG_SIGNING_KEY` is complete (includes `-----BEGIN/END PGP PRIVATE KEY BLOCK-----`)
- **Auth errors**: Check `CENTRAL_TOKEN_USERNAME` and `CENTRAL_TOKEN_PASSWORD`
- **Version conflicts**: Maven Central versions are immutable; increment and re-release
- **Workflow fails on push**: Ensure GitHub Actions has write permissions (**Settings** → **Actions** → **General** → **Workflow permissions**)

## Supply-chain artifacts

Each release produces, in addition to the GPG-signed jars:

- **SBOM** (CycloneDX `*-sbom.json` / `*-sbom.xml`) per module, attached to the GitHub release, so consumers can inventory/scan transitive dependencies.
- **Build provenance** attestation (`actions/attest-build-provenance`) for the jars, verifiable with `gh attestation verify <jar> --repo java-helpers/simple-builders`.
- **Reproducible builds**: `project.build.outputTimestamp` is set so archive entries are deterministic. The release workflow updates it automatically; it can be overridden per build with `-Dproject.build.outputTimestamp=<commit ISO-8601 date>`.

## Notes

- Project uses [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH)
- The `main` branch must always be on a `-SNAPSHOT` version; the release version is derived automatically
- After each release, all modules are bumped to the next minor snapshot (e.g., `0.5.0` → `0.6.0-SNAPSHOT`)
- Versions with `-` (e.g., `0.2.0-beta`) are marked as pre-releases
- Releases are **staged** to the Sonatype Central portal and require a **manual publish** step; nothing is auto-released
- Only `core` and `processor` modules are published to Maven Central
- The `example` and `example-custom-generator` modules are version-updated alongside the released modules but are **not** deployed to Maven Central
