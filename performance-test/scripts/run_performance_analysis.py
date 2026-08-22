#!/usr/bin/env python3
"""Run the performance-test compilation N times and aggregate JSON reports.

Usage:
    python3 scripts/run_performance_analysis.py [runs]

    runs  - number of compilation runs (default: 10)

Each run does a clean compile with performanceTracking enabled and writes
a JSON report to target/performance-reports/run-<N>.json. After all runs,
an aggregated summary is written to target/performance-reports/summary.json.
"""

import json
import shutil
import subprocess
import sys
import time
from pathlib import Path
from typing import Optional

BASE_DIR = Path(__file__).resolve().parent.parent
REPORT_DIR = BASE_DIR / "target" / "performance-reports"


def run_one(run_index: int) -> Optional[dict]:
    """Run a single clean compile and return the parsed JSON report."""
    report_file = REPORT_DIR / f"run-{run_index:02d}.json"

    print(f"  Run {run_index}: compiling...", flush=True)
    start = time.time()

    result = subprocess.run(
        [
            "mvn", "clean", "compile",
            "-Dsimplebuilder.performanceTracking=true",
            f"-Dsimplebuilder.performanceOutputFile={report_file}",
            "-q",
        ],
        cwd=BASE_DIR,
        capture_output=True,
        text=True,
    )

    elapsed = time.time() - start

    if result.returncode != 0:
        print(f"  Run {run_index}: FAILED (exit {result.returncode}, {elapsed:.1f}s)")
        print(result.stderr[-500:] if result.stderr else "(no stderr)")
        return None

    if not report_file.exists():
        print(f"  Run {run_index}: compiled OK but no JSON report found ({elapsed:.1f}s)")
        return None

    with report_file.open() as f:
        data = json.load(f)

    print(
        f"  Run {run_index}: OK - {data['totalClasses']} classes, "
        f"{data['totalProcessingTimeSeconds']}s processor, "
        f"{elapsed:.1f}s total wall time",
        flush=True,
    )
    return data


def aggregate(runs: list[dict]) -> dict:
    """Aggregate results from multiple runs into a summary."""
    n = len(runs)
    if n == 0:
        return {"error": "no successful runs"}

    total_times = [r["totalProcessingTimeSeconds"] for r in runs]
    avg_times = [r["averagePerClassMs"] for r in runs]
    wall_times = [r.get("_wallTimeSeconds", 0) for r in runs]

    summary = {
        "runCount": n,
        "processorTime": {
            "min": min(total_times),
            "max": max(total_times),
            "avg": sum(total_times) / n,
            "values": total_times,
        },
        "averagePerClassMs": {
            "min": min(avg_times),
            "max": max(avg_times),
            "avg": sum(avg_times) / n,
            "values": avg_times,
        },
        "wallTime": {
            "min": min(wall_times),
            "max": max(wall_times),
            "avg": sum(wall_times) / n,
            "values": wall_times,
        },
        "totalClasses": runs[0]["totalClasses"],
    }

    # Aggregate phase breakdown (average elapsedNanos across runs)
    first_phases = runs[0]["phaseBreakdown"]
    phase_avgs = {}  # type: dict[str, float]

    def collect_phase_avg(phases: dict, all_runs_phases: list[dict], path: str = ""):
        for phase_name, phase_data in phases.items():
            full_path = f"{path}.{phase_name}" if path else phase_name
            nanos_list = []
            for run_phases in all_runs_phases:
                p = run_phases
                for part in full_path.split("."):
                    if part in p:
                        p = p[part]
                    elif "children" in p and part in p["children"]:
                        p = p["children"][part]
                    else:
                        p = None
                        break
                if p and "elapsedNanos" in p:
                    nanos_list.append(p["elapsedNanos"])
            if nanos_list:
                phase_avgs[full_path] = sum(nanos_list) / len(nanos_list)
            children = phase_data.get("children")
            if children:
                collect_phase_avg(children, [rp.get("phaseBreakdown", {}) for rp in runs], full_path)

    collect_phase_avg(first_phases, [r["phaseBreakdown"] for r in runs])
    summary["phaseAverageNanos"] = phase_avgs

    # Aggregate top 10 slowest classes (by average elapsedMs across runs)
    class_times = {}  # type: dict[str, list[float]]
    class_info: dict[str, dict] = {}
    for run in runs:
        for cm in run["classMetrics"]:
            name = cm["className"]
            class_times.setdefault(name, []).append(cm["elapsedMs"])
            class_info[name] = {
                "fieldCount": cm["fieldCount"],
                "collectionCount": cm["collectionCount"],
            }

    class_avgs = []
    for name, times in class_times.items():
        class_avgs.append({
            "className": name,
            "avgMs": sum(times) / len(times),
            "minMs": min(times),
            "maxMs": max(times),
            "fieldCount": class_info[name]["fieldCount"],
            "collectionCount": class_info[name]["collectionCount"],
        })
    class_avgs.sort(key=lambda x: x["avgMs"], reverse=True)
    summary["topClassesByAvg"] = class_avgs[:20]

    # Aggregate generator stats
    gen_times = {}  # type: dict[str, dict[str, list]]
    for run in runs:
        for gs in run["generatorStats"]:
            name = gs["name"]
            gen_times.setdefault(name, {"elapsedNanos": [], "calls": [], "avgMsPerCall": []})
            gen_times[name]["elapsedNanos"].append(gs["elapsedNanos"])
            gen_times[name]["calls"].append(gs["calls"])
            gen_times[name]["avgMsPerCall"].append(gs["avgMsPerCall"])

    summary["generatorStats"] = []
    for name, vals in gen_times.items():
        summary["generatorStats"].append({
            "name": name,
            "avgElapsedNanos": sum(vals["elapsedNanos"]) / len(vals["elapsedNanos"]),
            "avgCalls": sum(vals["calls"]) / len(vals["calls"]),
            "avgMsPerCall": sum(vals["avgMsPerCall"]) / len(vals["avgMsPerCall"]),
        })
    summary["generatorStats"].sort(key=lambda x: x["avgElapsedNanos"], reverse=True)

    # Aggregate enhancer stats
    enh_times = {}  # type: dict[str, dict[str, list]]
    for run in runs:
        for es in run["enhancerStats"]:
            name = es["name"]
            enh_times.setdefault(name, {"elapsedNanos": [], "calls": [], "avgMsPerCall": []})
            enh_times[name]["elapsedNanos"].append(es["elapsedNanos"])
            enh_times[name]["calls"].append(es["calls"])
            enh_times[name]["avgMsPerCall"].append(es["avgMsPerCall"])

    summary["enhancerStats"] = []
    for name, vals in enh_times.items():
        summary["enhancerStats"].append({
            "name": name,
            "avgElapsedNanos": sum(vals["elapsedNanos"]) / len(vals["elapsedNanos"]),
            "avgCalls": sum(vals["calls"]) / len(vals["calls"]),
            "avgMsPerCall": sum(vals["avgMsPerCall"]) / len(vals["avgMsPerCall"]),
        })
    summary["enhancerStats"].sort(key=lambda x: x["avgElapsedNanos"], reverse=True)

    return summary


def main() -> None:
    num_runs = int(sys.argv[1]) if len(sys.argv) > 1 else 10

    if REPORT_DIR.exists():
        shutil.rmtree(REPORT_DIR)
    REPORT_DIR.mkdir(parents=True)

    print(f"Running {num_runs} performance analysis runs...")
    print(f"Report directory: {REPORT_DIR}")
    print()

    runs: list[dict] = []
    for i in range(1, num_runs + 1):
        run_start = time.time()
        data = run_one(i)
        if data is not None:
            data["_wallTimeSeconds"] = time.time() - run_start
            runs.append(data)

    print()
    print(f"Successful runs: {len(runs)}/{num_runs}")

    if not runs:
        print("No successful runs to aggregate.")
        sys.exit(1)

    summary = aggregate(runs)
    summary_file = REPORT_DIR / "summary.json"
    with summary_file.open("w") as f:
        json.dump(summary, f, indent=2)

    print()
    print("=== Summary ===")
    print(f"  Runs: {summary['runCount']}")
    print(f"  Classes per run: {summary['totalClasses']}")
    print(f"  Processor time avg: {summary['processorTime']['avg']:.1f}s "
          f"(min: {summary['processorTime']['min']:.1f}s, max: {summary['processorTime']['max']:.1f}s)")
    print(f"  Avg per class: {summary['averagePerClassMs']['avg']:.1f}ms "
          f"(min: {summary['averagePerClassMs']['min']:.1f}ms, max: {summary['averagePerClassMs']['max']:.1f}ms)")
    print(f"  Wall time avg: {summary['wallTime']['avg']:.1f}s")
    print()
    print("  Top 5 classes by avg time:")
    for c in summary["topClassesByAvg"][:5]:
        print(f"    {c['className']}: {c['avgMs']:.1f}ms avg "
              f"({c['minMs']:.1f}-{c['maxMs']:.1f}ms, {c['fieldCount']} fields, {c['collectionCount']} collections)")
    print()
    print(f"Summary written to: {summary_file}")


if __name__ == "__main__":
    main()
