# Performance Analysis Guide

This guide explains how to run performance analysis for different builder
frameworks using the scripts in the `performance-test` module.

## Overview

The performance test module generates a large set of Java classes (records and
plain classes) from a JSON catalog, then compiles them with a builder annotation
processor to measure processing time. Three scripts work together:

| Script | Purpose |
|---|---|
| `generate_classes.py` | Generate Java source files with a chosen builder annotation |
| `run_performance_measurement.py` | Run N compilations and aggregate timing results |
| `compare_performance.py` | Compare results from multiple measurement runs side-by-side |
| `run_full_comparison.py` | Run all frameworks end-to-end and compare (convenience) |

## Supported Builder Types

| `--builder-type` | Annotation | Maven Profile | JSON Report | Notes |
|---|---|---|---|---|
| `simple-builder` | `@SimpleBuilder` | `simplebuilder` | Yes | Full feature set (default) |
| `simple-minimal-builder` | `@MinimalBuilder` | `minimalbuilder` | Yes | All optional features disabled |
| `record-builder` | `@RecordBuilder` | `recordbuilder` | No | RecordBuilder library (wall-time only) |
| `lombok` | `@Builder` | `lombok` | No | Lombok (wall-time only) |

Builder types with JSON reports produce detailed phase/class/generator/enhancer
metrics. Builder types without JSON reports only measure overall wall time.

## Quick Start

Run all four frameworks with N runs each, then compare:

```bash
python3 scripts/run_full_comparison.py --runs 10
```

Use `--keep-builders` to copy generated builders to `generated-builders/<type>/`
so they survive Maven clean:

```bash
python3 scripts/run_full_comparison.py --runs 30 --keep-builders
```

Labels are auto-generated as `sb-<N>runs`, `mb-<N>runs`, `rb-<N>runs`,
`lombok-<N>runs`.

### Running Individual Frameworks

For single-framework measurements, use the individual scripts directly.
Use `--force` when regenerating classes after switching builder types.

```bash
python3 scripts/generate_classes.py --builder-type simple-builder --force
python3 scripts/run_performance_measurement.py --runs 30 --label sb-30runs --builder-type simple-builder
```

Results are written to `performance-test/performance-reports/<label>/`. Compare paths
are relative to that directory (or use absolute paths). The parent directory
name is used as the column label.

When comparing JSON-profile runs with wall-time-only runs, the script
automatically shows wall time for all summaries and detailed metrics
(processor time, phases, classes, generators, enhancers) only when
available.

## Output File Locations

```
performance-test/
└── performance-reports/
    ├── sb-30runs/
    │   ├── run-01.json
    │   ├── run-02.json
    │   ├── ...
    │   └── summary.json
    ├── mb-30runs/
    │   ├── run-01.json
    │   ├── ...
    │   └── summary.json
    ├── rb-30runs/
    │   └── summary.json       (wall-time only)
    └── lombok-30runs/
        └── summary.json       (wall-time only)
```

## Summary JSON Structure

### JSON-Profile Summaries (simple-builder, simple-minimal-builder)

```json
{
  "runCount": 30,
  "wallTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "sourceFileCount": 1077,
  "generatedBuilderCount": 1077,
  "compilerTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "compilerTimePerBuilderMs": { "min": ..., "max": ..., "avg": ... },
  "processorTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "averagePerClassMs": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "totalClasses": 1077,
  "phaseAverageNanos": { "Phase.Name": avgNanos, ... },
  "topClassesByAvg": [
    { "className": ..., "avgMs": ..., "minMs": ..., "maxMs": ...,
      "fieldCount": ..., "collectionCount": ... }
  ],
  "generatorStats": [
    { "name": ..., "avgElapsedNanos": ..., "avgCalls": ..., "avgMsPerCall": ... }
  ],
  "enhancerStats": [
    { "name": ..., "avgElapsedNanos": ..., "avgCalls": ..., "avgMsPerCall": ... }
  ]
}
```

### Wall-Time-Only Summaries (record-builder, lombok)

```json
{
  "runCount": 30,
  "wallTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "sourceFileCount": 1077,
  "generatedBuilderCount": 1077,
  "compilerTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "compilerTimePerBuilderMs": { "min": ..., "max": ..., "avg": ... },
  "wallTimeOnly": true
}
```

## Benchmark Results

The following results are from a 10-iteration stability run using
[`run_full_comparison.py`](../scripts/run_full_comparison.py)
`--runs 10 --no-tracking --label-suffix stability`.
All frameworks were measured with wall-time only (no JSON tracking overhead) to
ensure a fair comparison. Note that the builder counts differ: Simple Builders
and Lombok generate one builder per class, while RecordBuilder generates fewer
(295) because the test dataset contains fewer records than plain classes.

### Wall-Time Comparison (10 runs, wall-time only)

| Framework | Builders | Wall Avg (s) | Wall Min (s) | Wall Max (s) | Per Builder (ms) |
|-----------|----------|-------------|-------------|-------------|-----------------|
| Simple Builder (`@SimpleBuilder`) | 1079 | 53.1 | 52.8 | 53.4 | 48.0 |
| Simple Minimal Builder (`@SimpleMinimalBuilder`) | 1079 | 22.0 | 21.5 | 22.4 | 19.3 |
| RecordBuilder (`@RecordBuilder`) | 295 | 6.6 | 6.5 | 6.7 | 18.5 |
| Lombok (`@Builder`) | 1077 | 6.9 | 6.7 | 7.1 | 5.3 |

Key observations:

- **Lombok** is fastest per builder but instruments bytecode at compile time
  rather than generating separate source files, so the comparison is not
  apples-to-apples.
- **RecordBuilder** generates far fewer builders (295 vs 1079) because the test
  dataset contains fewer records than plain classes. However, its per-builder
  cost (~18.5 ms) is nearly identical to Simple Minimal Builder (~19.3 ms) — the
  wall-time difference is almost entirely due to the lower builder count, not
  per-builder efficiency.
- **Simple Minimal Builder** is ~2.4x faster than Simple Builder. The speedup
  comes from two factors: fewer generated methods (no collection helpers,
  conditional logic, supplier/consumer setters, Javadoc, etc.) and
  correspondingly less source formatting work.
- **Simple Builder** is the slowest due to its full feature set. The per-builder
  cost (~48 ms) is dominated by code generation and formatting.

### Processor-Internal Breakdown (with JSON tracking)

For deeper insight into where time is spent inside the Simple Builders
processor, enable the processor's internal performance tracker with
`-Asimplebuilder.performanceTracking=true`. This adds minor overhead but
provides phase-level breakdowns in the JSON report.

A representative 10-run measurement of Simple Builder (full features, JDT
formatting) shows the following phase distribution:

| Phase | Avg (s) | Share |
|-------|---------|-------|
| Config Resolution | 0.08 | <1% |
| Builder Def Extraction | 0.73 | ~2% |
| DTO Mapping | 0.03 | <1% |
| Code Generation | 42.3 | ~93% |
| **Processor Total** | **45.3** | |
| **Wall Total** | **54.0** | |

Code generation dominates at ~93% of processor time. This includes Roaster
source construction and source formatting. A 5-run comparison of the three
formatting modes (Simple Builder, 1079 builders) shows the following:

| Formatting Mode | Wall Avg (s) | Per Builder (ms) | vs NONE |
|-----------------|-------------|-----------------|---------|
| NONE (raw Roaster) | 74.8 | 67.6 | — |
| LIGHTWEIGHT | 78.5 | 71.1 | +5% |
| JDT (default) | 81.8 | 74.1 | +9% |

Formatting adds ~7s total (~6.5 ms/builder) for JDT over NONE. The lightweight
formatter adds about half that cost. While measurable, formatting is a
secondary factor compared to the difference in generated method count between
Simple Builder and Simple Minimal Builder (~31s), which is driven primarily by
the number of methods and collection helpers generated.

### Running the Benchmarks

To reproduce the wall-time comparison, see [Quick Start](#quick-start) above.
For processor-internal breakdowns, run
[`run_performance_measurement.py`](../scripts/run_performance_measurement.py)
without `--no-tracking` to enable JSON reporting.
