#!/usr/bin/env python3
"""Compare performance summary JSON files.

Usage:
    python3 scripts/compare_performance.py [options] FILE1 [FILE2] ...

Examples:
    # Compare 3-run vs 30-run simplebuilder results
    python3 scripts/compare_performance.py \
        3runs/summary.json \
        30runs/summary.json

    # Compare simplebuilder vs lombok (wall-time only)
    python3 scripts/compare_performance.py \
        sb-30runs/summary.json \
        lombok-30runs/summary.json

    # Compare three frameworks
    python3 scripts/compare_performance.py \
        sb-30runs/summary.json \
        rb-30runs/summary.json \
        lombok-30runs/summary.json

Options:
    --top-classes N    Number of top classes to show (default: 10)
    --top-generators N Number of top generators to show (default: 5)
    --top-enhancers N  Number of top enhancers to show (default: 5)

Each FILE argument is a path to a summary.json file. The parent directory
name is used as the column label (e.g. `sb-30runs/summary.json` → label
`sb-30runs`). The script auto-discovers phase names from the data and
gracefully handles summaries that lack phase/class/generator/enhancer
data (e.g. wall-time-only runs from RecordBuilder or Lombok).
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent / "performance-reports"


def load_summary(filepath: str) -> dict | None:
    """Load a summary JSON file, returning None on error."""
    path = Path(filepath)
    if not path.is_absolute():
        path = BASE / path
    if not path.exists():
        print(f"Warning: file not found: {path}", file=sys.stderr)
        return None
    with path.open() as f:
        try:
            return json.load(f)
        except json.JSONDecodeError as e:
            print(f"Error: invalid JSON in {path}: {e}", file=sys.stderr)
            return None


def collect_phase_names(summaries: list[tuple[str, dict]]) -> list[str]:
    """Collect all unique phase names across all summaries, preserving order."""
    phases: list[str] = []
    seen: set[str] = set()
    for _label, s in summaries:
        phase_avgs = s.get("phaseAverageNanos", {})
        for p in phase_avgs:
            if p not in seen:
                seen.add(p)
                phases.append(p)
    return phases


def shorten_phase(name: str) -> str:
    """Abbreviate long phase names for compact display."""
    return (
        name.replace("Code Generation.", "CG.")
        .replace("Source Construction.", "SC.")
        .replace("Element Building.", "EB.")
        .replace("Configuration Resolution", "Config Resolution")
        .replace("Builder Definition Extraction", "Builder Def Extraction")
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Compare performance summary JSON files.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "files",
        nargs="+",
        help="Paths to summary.json files (e.g. sb-30runs/summary.json)",
    )
    parser.add_argument(
        "--top-classes",
        type=int,
        default=10,
        help="Number of top classes to show (default: 10)",
    )
    parser.add_argument(
        "--top-generators",
        type=int,
        default=5,
        help="Number of top generators to show (default: 5)",
    )
    parser.add_argument(
        "--top-enhancers",
        type=int,
        default=5,
        help="Number of top enhancers to show (default: 5)",
    )
    args = parser.parse_args()

    # Derive labels from parent directory names
    entries: list[tuple[str, str]] = []
    for f in args.files:
        p = Path(f)
        label = p.parent.name if p.parent.name else p.name
        entries.append((f, label))

    # Load all summaries
    summaries: list[tuple[str, dict]] = []
    for filepath, label in entries:
        s = load_summary(filepath)
        if s is None:
            print(f"Error: could not load summary for '{label}' from {filepath}", file=sys.stderr)
            sys.exit(1)
        summaries.append((label, s))

    if not summaries:
        print("Error: no valid summary files loaded.", file=sys.stderr)
        sys.exit(1)

    labels = [label for label, _ in summaries]
    n = len(labels)

    # Determine which sections are available
    has_processor_time = all("processorTime" in s for _, s in summaries)
    has_per_class = all("averagePerClassMs" in s for _, s in summaries)
    has_phases = all("phaseAverageNanos" in s for _, s in summaries)
    has_classes = any("topClassesByAvg" in s for _, s in summaries)
    has_generators = any("generatorStats" in s for _, s in summaries)
    has_enhancers = any("enhancerStats" in s for _, s in summaries)

    # Header
    print("=" * 80)
    print(f"PERFORMANCE COMPARISON: {' vs '.join(labels)}")
    print("=" * 80)
    print()

    # Total classes (from first summary that has it)
    for label, s in summaries:
        if "totalClasses" in s:
            print(f"Classes per run ({label}): {s['totalClasses']}")
            break
    if not has_processor_time:
        wall_only_labels = [label for label, s in summaries if s.get("wallTimeOnly", False)]
        if wall_only_labels:
            print(f"Wall-time-only (no JSON): {', '.join(wall_only_labels)}")
    print()

    # --- Source & Builder Counts (always available) ---
    print("--- Source & Builder Counts ---")
    count_header = "{:<24}" + " {:>12}" * n
    count_row = "{:<24}" + " {:>12}" * n
    print(count_header.format("Metric", *labels))
    print(count_row.format("Source files",
          *[s.get("sourceFileCount", 0) for _, s in summaries]))
    print(count_row.format("Generated builders",
          *[s.get("generatedBuilderCount", 0) for _, s in summaries]))
    # For JSON builder types, totalClasses is the processor's own count
    tc_vals = [s.get("totalClasses", "-") for _, s in summaries]
    print(count_row.format("Processor-reported", *tc_vals))
    print()

    # --- Wall Time (always available) ---
    print("--- Wall Time (seconds) ---")
    header = "{:<16}" + " {:>12}" * n
    row = "{:<16}" + " {:>12.1f}" * n
    print(header.format("Metric", *labels))
    print(row.format("Min", *[s["wallTime"]["min"] for _, s in summaries]))
    print(row.format("Max", *[s["wallTime"]["max"] for _, s in summaries]))
    print(row.format("Avg", *[s["wallTime"]["avg"] for _, s in summaries]))
    print()

    # --- Compiler Time (from Maven timestamps, available for all types) ---
    has_compiler_time = any("compilerTime" in s for _, s in summaries)
    if has_compiler_time:
        print("--- Compiler Time (seconds, from Maven timestamps) ---")
        print(header.format("Metric", *labels))
        ct = lambda key: [s.get("compilerTime", {}).get(key, 0.0) for _, s in summaries]
        print(row.format("Min", *ct("min")))
        print(row.format("Max", *ct("max")))
        print(row.format("Avg", *ct("avg")))
        print()

    # --- Compiler Time per Builder (ms, comparable across builder types) ---
    has_per_builder = any("compilerTimePerBuilderMs" in s for _, s in summaries)
    if has_per_builder:
        print("--- Compiler Time per Builder (ms) ---")
        print(header.format("Metric", *labels))
        ctpb = lambda key: [s.get("compilerTimePerBuilderMs", {}).get(key, 0.0) for _, s in summaries]
        print(row.format("Min", *ctpb("min")))
        print(row.format("Max", *ctpb("max")))
        print(row.format("Avg", *ctpb("avg")))
        print()

    # --- Processor Time ---
    if has_processor_time:
        print("--- Processor Time (seconds) ---")
        print(header.format("Metric", *labels))
        print(row.format("Min", *[s["processorTime"]["min"] for _, s in summaries]))
        print(row.format("Max", *[s["processorTime"]["max"] for _, s in summaries]))
        print(row.format("Avg", *[s["processorTime"]["avg"] for _, s in summaries]))
        print()

    # --- Average per Class ---
    if has_per_class:
        print("--- Average per Class (ms) ---")
        print(header.format("Metric", *labels))
        print(row.format("Min", *[s["averagePerClassMs"]["min"] for _, s in summaries]))
        print(row.format("Max", *[s["averagePerClassMs"]["max"] for _, s in summaries]))
        print(row.format("Avg", *[s["averagePerClassMs"]["avg"] for _, s in summaries]))
        print()

    # --- Phase Averages ---
    if has_phases:
        phases = collect_phase_names(summaries)
        if phases:
            print("--- Phase Average (seconds) ---")
            phase_header = "{:<50}" + " {:>10}" * n
            phase_row = "{:<50}" + " {:>9.2f}s" * n
            print(phase_header.format("Phase", *labels))
            for p in phases:
                vals = []
                for _, s in summaries:
                    v = s.get("phaseAverageNanos", {}).get(p, 0) / 1e9
                    vals.append(v)
                short = shorten_phase(p)
                print(phase_row.format(short, *vals))
            print()

    # --- Top Classes ---
    if has_classes:
        # Use the first summary that has class data as the reference
        ref_label, ref_s = next((l, s) for l, s in summaries if "topClassesByAvg" in s)
        print(f"--- Top {args.top_classes} Classes by Avg Time (from {ref_label}) ---")
        cls_header = "{:<3} {:<30}" + " {:>12}" * n
        cls_row = "{:<3} {:<30}" + " {:>12.1f}" * n
        print(cls_header.format("#", "Class", *[f"{l} avg" for l in labels]))
        for i, cm in enumerate(ref_s["topClassesByAvg"][:args.top_classes]):
            name = cm["className"]
            vals = []
            for label, s in summaries:
                avg = 0
                for c in s.get("topClassesByAvg", []):
                    if c["className"] == name:
                        avg = c["avgMs"]
                        break
                vals.append(avg)
            print(cls_row.format(i + 1, name, *vals))
        print()

    # --- Generator Stats ---
    if has_generators:
        ref_label, ref_s = next((l, s) for l, s in summaries if "generatorStats" in s)
        print(f"--- Generator Stats (from {ref_label}, avg) ---")
        gen_header = "{:<3} {:<35}" + " {:>12}" * n
        gen_row = "{:<3} {:<35}" + " {:>12.3f}" * n
        print(gen_header.format("#", "Generator", *[f"{l} ms/call" for l in labels]))
        for i, gs in enumerate(ref_s["generatorStats"][:args.top_generators]):
            name = gs["name"]
            vals = []
            for label, s in summaries:
                v = 0
                for g in s.get("generatorStats", []):
                    if g["name"] == name:
                        v = g["avgMsPerCall"]
                        break
                vals.append(v)
            print(gen_row.format(i + 1, name, *vals))
        print()

    # --- Enhancer Stats ---
    if has_enhancers:
        ref_label, ref_s = next((l, s) for l, s in summaries if "enhancerStats" in s)
        print(f"--- Enhancer Stats (from {ref_label}, avg) ---")
        enh_header = "{:<3} {:<35}" + " {:>12}" * n
        enh_row = "{:<3} {:<35}" + " {:>12.3f}" * n
        print(enh_header.format("#", "Enhancer", *[f"{l} ms/call" for l in labels]))
        for i, es in enumerate(ref_s["enhancerStats"][:args.top_enhancers]):
            name = es["name"]
            vals = []
            for label, s in summaries:
                v = 0
                for e in s.get("enhancerStats", []):
                    if e["name"] == name:
                        v = e["avgMsPerCall"]
                        break
                vals.append(v)
            print(enh_row.format(i + 1, name, *vals))


if __name__ == "__main__":
    main()
