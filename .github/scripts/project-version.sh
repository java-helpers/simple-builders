#!/usr/bin/env bash
#
# Sets or verifies the version of all Maven POMs in this repository.
#
# The modules have no <parent>, so every POM carries its own version. The POMs
# are found via git, so new or profile-only modules (e.g. performance-test)
# are always included.

set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0") --set <version> | --verify

  --set <version>  set <version> in all POMs
  --verify         fail if any POM differs from the version of the root POM
  -h, --help       show this help
USAGE
}

all_poms() {
  git ls-files pom.xml '*/pom.xml'
}

pom_version() {
  mvn -B -q -N -f "$1" help:evaluate -Dexpression=project.version -DforceStdout
}

set_version() {
  local version="$1"
  for pom in $(all_poms); do
    echo "Setting version of $pom to $version"
    # The reproducible build timestamp is pinned by the release workflow itself.
    mvn -B -q -N -f "$pom" versions:set \
      -DnewVersion="$version" \
      -DgenerateBackupPoms=false \
      -DupdateBuildOutputTimestampPolicy=never
  done
}

verify_versions() {
  local expected actual failed=0
  expected="$(pom_version pom.xml)"
  for pom in $(all_poms); do
    actual="$(pom_version "$pom")"
    if [[ "$actual" != "$expected" ]]; then
      echo "::error file=$pom::$pom has version $actual, expected $expected"
      failed=1
    fi
  done
  return "$failed"
}

option="${1:-}"

if [[ "$option" == "--set" && $# -eq 2 ]]; then
  set_version "$2"
elif [[ "$option" == "--verify" && $# -eq 1 ]]; then
  verify_versions
elif [[ "$option" == "-h" || "$option" == "--help" ]]; then
  usage
else
  usage >&2
  exit 1
fi
