#!/usr/bin/env zsh
# Run stability analysis: 4 builder types × 3 run counts (5, 10, 15)
# Then compare each builder type across run counts to assess statistical stability.
#
# Usage:
#   zsh performance-test/scripts/run_stability_analysis.sh
#
# Output directories:
#   performance-reports/{sb,mb,rb,lombok}-{5,10,15}runs-stability/
#
# Expected runtime: ~20-25 minutes

set -euo pipefail

echo "============================================================"
echo "  Stability analysis started: $(date '+%Y-%m-%d %H:%M:%S')"
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
echo "  STABILITY ANALYSIS: 4 builders × 3 run counts (5/10/15)"
echo "============================================================"
echo "  macOS optimizations:"
echo "    - System sleep disabled (caffeinate)"
echo "    - Process priority elevated (renice -10)"
echo "  For maximum performance, also consider:"
echo "    - Close other CPU-intensive apps (browsers, IDEs)"
echo "    - Ensure adequate cooling (laptop on hard surface)"
echo "    - Plug in power adapter (prevents thermal throttling)"
echo "============================================================"
echo

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

for RUNS in 5 10 15; do
  echo "============================================================"
  echo "  Running all 4 builders with ${RUNS} runs (--no-tracking)"
  echo "============================================================"
  python3 performance-test/scripts/run_full_comparison.py \
    --runs "$RUNS" --no-tracking --label-suffix stability
  echo
done

echo "============================================================"
echo "  STABILITY COMPARISONS (same builder across run counts)"
echo "============================================================"
echo

echo "=== STABILITY: simple-builder (5 vs 10 vs 15) ==="
python3 performance-test/scripts/compare_performance.py \
  sb-5runs-stability/summary.json \
  sb-10runs-stability/summary.json \
  sb-15runs-stability/summary.json
echo

echo "=== STABILITY: simple-minimal-builder (5 vs 10 vs 15) ==="
python3 performance-test/scripts/compare_performance.py \
  mb-5runs-stability/summary.json \
  mb-10runs-stability/summary.json \
  mb-15runs-stability/summary.json
echo

echo "=== STABILITY: record-builder (5 vs 10 vs 15) ==="
python3 performance-test/scripts/compare_performance.py \
  rb-5runs-stability/summary.json \
  rb-10runs-stability/summary.json \
  rb-15runs-stability/summary.json
echo

echo "=== STABILITY: lombok (5 vs 10 vs 15) ==="
python3 performance-test/scripts/compare_performance.py \
  lombok-5runs-stability/summary.json \
  lombok-10runs-stability/summary.json \
  lombok-15runs-stability/summary.json
echo

echo "============================================================"
echo "  Stability analysis complete."
echo "  Started:  $(date -r "$START_TIME" '+%Y-%m-%d %H:%M:%S')"
echo "  Finished: $(date '+%Y-%m-%d %H:%M:%S')"
echo "  Reports: performance-test/performance-reports/*-stability/"
echo "============================================================"
