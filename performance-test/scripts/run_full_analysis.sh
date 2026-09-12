#!/usr/bin/env zsh
# Run the full performance analysis for simple-builders.
#
# This is the single entry point referenced in PERFORMANCE_ANALYSIS.md.
# It runs two analyses:
#
#   1. Cross-framework comparison (wall-time only):
#      simple-builder, simple-minimal-builder, record-builder, lombok
#      All measured with --no-tracking for a fair wall-time comparison.
#
#   2. Formatting-mode analysis (with processor metrics):
#      simple-builder with jdt, lightweight, and none formatting modes.
#      Tracking is enabled so JSON processor metrics are available for
#      deeper insight into formatting overhead.
#
# Usage:
#   zsh performance-test/scripts/run_full_analysis.sh
#   RUNS=5 zsh performance-test/scripts/run_full_analysis.sh   # override run count (default: 10)
#
# Output directories:
#   performance-reports/{sb,mb,rb,lombok}-{N}runs/
#   performance-reports/fmt-{jdt,lightweight,none}-{N}runs/
#
# Expected runtime: ~30-40 minutes (depends on RUNS and hardware)

set -euo pipefail

RUNS="${RUNS:-10}"

echo "============================================================"
echo "  FULL PERFORMANCE ANALYSIS started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "  Runs per measurement: ${RUNS}"
echo "============================================================"
echo

START_TIME=$(date +%s)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$BASE_DIR/.."

# --- macOS performance optimizations ---
# Prevent system sleep while the analysis is running
if command -v caffeinate &>/dev/null; then
  caffeinate -dimsu -w $$ &
fi

# Run the script and all children at elevated priority
renice -n -10 -p $$ 2>/dev/null || true

echo "============================================================"
echo "  JVM / BUILD CONTEXT"
echo "============================================================"
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1)
JVM_FLAGS=$(java -XX:+PrintFlagsFinal -version 2>/dev/null)
HEAP_MAX=$(echo "$JVM_FLAGS" | awk '/MaxHeapSize/ {printf "%.0f", $4/1024/1024; exit}')
HEAP_INIT=$(echo "$JVM_FLAGS" | awk '/InitialHeapSize/ {printf "%.0f", $4/1024/1024; exit}')
META_MAX=$(echo "$JVM_FLAGS" | awk '/MaxMetaspaceSize/ {
  if ($4 == "18446744073709551615") {
    print "unlimited"
  } else {
    printf "%.0fm", $4/1024/1024
  }
  exit
}')
GC=$(java -XX:+PrintCommandLineFlags -version 2>&1 | tr ' ' '\n' | grep '^-XX:+Use.*GC' | sed -e 's/-XX:+Use//' -e 's/GC$//' | head -n 1)
echo "  Java:      $JAVA_VERSION"
echo "  Maven:     $MAVEN_VERSION"
echo "  Heap:      -Xms=${HEAP_INIT}m -Xmx=${HEAP_MAX}m"
echo "  Metaspace: -XX:MaxMetaspaceSize=${META_MAX}"
echo "  GC:        ${GC:-default}"
echo "============================================================"
echo

# ============================================================
# Part 1: Cross-framework comparison (wall-time only)
# ============================================================
echo "============================================================"
echo "  PART 1: Cross-framework comparison (${RUNS} runs, --no-tracking)"
echo "  Builders: simple-builder, simple-minimal-builder, record-builder, lombok"
echo "============================================================"
echo

python3 performance-test/scripts/run_full_comparison.py \
  --runs "$RUNS" --no-tracking
echo

# ============================================================
# Part 2: Formatting-mode analysis (with processor metrics)
# ============================================================
echo "============================================================"
echo "  PART 2: Formatting-mode analysis (${RUNS} runs, with tracking)"
echo "  Builder: simple-builder"
echo "  Modes:   jdt, lightweight, none"
echo "============================================================"
echo

# Generate simple-builder sources once for all formatting-mode runs
python3 performance-test/scripts/generate_classes.py \
  --builder-type simple-builder --force
echo

for MODE in jdt lightweight none; do
  echo "============================================================"
  echo "  simple-builder, formattingMode=${MODE}, ${RUNS} runs (with tracking)"
  echo "============================================================"
  python3 performance-test/scripts/run_performance_measurement.py \
    --runs "$RUNS" \
    --label "fmt-${MODE}-${RUNS}runs" \
    --builder-type simple-builder \
    --formatting-mode "$MODE"
  echo
done

echo "============================================================"
echo "  FORMATTING-MODE COMPARISON (with processor metrics)"
echo "============================================================"
echo
echo "=== FORMATTING MODES: jdt vs lightweight vs none ==="
python3 performance-test/scripts/compare_performance.py \
  "fmt-jdt-${RUNS}runs/summary.json" \
  "fmt-lightweight-${RUNS}runs/summary.json" \
  "fmt-none-${RUNS}runs/summary.json"
echo

# ============================================================
# Summary
# ============================================================
echo "============================================================"
echo "  Full performance analysis complete."
echo "  Started:  $(date -r "$START_TIME" '+%Y-%m-%d %H:%M:%S')"
echo "  Finished: $(date '+%Y-%m-%d %H:%M:%S')"
echo "  Reports:"
echo "    Cross-framework: performance-test/performance-reports/{sb,mb,rb,lombok}-${RUNS}runs/"
echo "    Formatting-mode: performance-test/performance-reports/fmt-{jdt,lightweight,none}-${RUNS}runs/"
echo "============================================================"
