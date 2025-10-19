# Release Process

This document describes how to release artifacts to Maven Central.

## Prerequisites

Configure these GitHub secrets in **Settings** → **Secrets and variables** → **Actions**:

1. **`CENTRAL_TOKEN_USERNAME`** - Maven Central token username
2. **`CENTRAL_TOKEN_PASSWORD`** - Maven Central token password
3. **`GPG_SIGNING_KEY`** - GPG private key (full armored block)
4. **`GPG_SIGNING_KEY_PASSWORD`** - GPG key passphrase

**First time setup?** See [MAVEN_CENTRAL_SETUP.md](MAVEN_CENTRAL_SETUP.md) for detailed instructions on:
- Creating a Maven Central account
- Verifying namespace ownership
- Generating publishing tokens
- Creating and exporting GPG keys

## How to Release

**Actions** → **Release to Maven Central** → **Run workflow** → Enter version (e.g., `0.2.0`)

### What the Workflow Does

1. Updates POM versions to release version
2. Commits changes and creates tag `v0.2.0`
3. Builds and verifies project with `-Prelease`
4. Signs artifacts with GPG
5. Deploys and **auto-publishes** to Maven Central
6. Pushes commit and tag to `main`
7. Creates **draft** GitHub release (requires manual publish)

## After Release

1. **Publish GitHub Release**: Go to **Releases** → Edit draft → **Publish release**
2. **Verify Maven Central**: Artifacts appear at https://central.sonatype.com/ (15-30 min delay)
   - Search for: `io.github.java-helpers:simple-builders-core` or `simple-builders-processor`
3. **Test the release**:
   ```xml
   <dependency>
     <groupId>io.github.java-helpers</groupId>
     <artifactId>simple-builders-core</artifactId>
     <version>0.2.0</version>
   </dependency>
   ```

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

## Notes

- Project uses [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH)
- Versions with `-` (e.g., `0.2.0-beta`) are marked as pre-releases
- Auto-publish is **only enabled in GitHub Actions** by default
- Both `core` and `processor` modules are published to Maven Central
- The `example` module is excluded from releases
