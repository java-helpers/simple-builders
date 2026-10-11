#!/usr/bin/env bash
# Keeps the version of every tracked pom.xml in sync with the root POM.
#
# The modules have no <parent> element, so each POM carries its own version.
# POMs are discovered via git instead of a hardcoded module list, so modules
# that are only part of a profile (e.g. performance-test) or added later are
# never left behind.
#
# project.build.outputTimestamp is left untouched; the release workflow pins
# it in a dedicated step.
#
# Usage:
#   project-version.sh set <version>   set <version> in every POM
#   project-version.sh verify          fail if any POM differs from the root POM
set -euo pipefail

POM_PATHSPEC=':(glob)**/pom.xml'

pom_version() {
  mvn -B -q -N -f "$1" help:evaluate -Dexpression=project.version -DforceStdout | tr -d '\r '
}

case "${1:-}" in
  set)
    NEW_VERSION="${2:?usage: $0 set <version>}"
    git ls-files -z -- "$POM_PATHSPEC" | while IFS= read -r -d '' pom; do
      echo "Setting $pom to $NEW_VERSION"
      mvn -B -q -N -f "$pom" versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false \
        -DupdateBuildOutputTimestampPolicy=never
    done
    ;;
  verify)
    ROOT_VERSION="$(pom_version pom.xml)"
    MISMATCHES=0
    while IFS= read -r -d '' pom; do
      VERSION="$(pom_version "$pom")"
      if [[ "$VERSION" != "$ROOT_VERSION" ]]; then
        echo "::error file=$pom::$pom is on version $VERSION, expected $ROOT_VERSION (root pom.xml)."
        MISMATCHES=$((MISMATCHES + 1))
      fi
    done < <(git ls-files -z -- "$POM_PATHSPEC")
    if [[ "$MISMATCHES" -gt 0 ]]; then
      echo "Run '.github/scripts/project-version.sh set $ROOT_VERSION' to align all POMs."
      exit 1
    fi
    echo "All POMs are on version $ROOT_VERSION."
    ;;
  *)
    echo "usage: $0 set <version> | verify" >&2
    exit 2
    ;;
esac
