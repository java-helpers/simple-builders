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

## Supported Builder Types

| `--builder-type` | Annotation | Maven Profile | JSON Report | Notes |
|---|---|---|---|---|
| `simple-builder` | `@SimpleBuilder` | `simplebuilder` | Yes | Full feature set (default) |
| `simple-minimal-builder` | `@MinimalBuilder` | `minimalbuilder` | Yes | All optional features disabled |
| `record-builder` | `@RecordBuilder` | `recordbuilder` | No | RecordBuilder library (wall-time only) |
| `lombok` | `@Builder` | `lombok` | No | Lombok (wall-time only) |

Builder types with JSON reports produce detailed phase/class/generator/enhancer
metrics. Builder types without JSON reports only measure overall wall time.

## Step-by-Step: Running an Analysis

### 1. Generate Classes

Generate Java source files with the desired builder annotation:

```bash
# SimpleBuilder (default)
python3 scripts/generate_classes.py

# MinimalBuilder
python3 scripts/generate_classes.py --builder-type simple-minimal-builder

# RecordBuilder
python3 scripts/generate_classes.py --builder-type record-builder

# Lombok
python3 scripts/generate_classes.py --builder-type lombok
```

**Important:** Always regenerate classes when switching builder types. The
generated source files use different annotations and imports for each framework.

Use `--force` to overwrite existing files:

```bash
python3 scripts/generate_classes.py --builder-type lombok --force
```

Use `--dry-run` to preview without writing:

```bash
python3 scripts/generate_classes.py --builder-type record-builder --dry-run
```

#### Custom Annotations

For annotations not covered by the presets, use individual parameters:

```bash
python3 scripts/generate_classes.py \
    --annotation MyBuilder \
    --annotation-import com.example.MyBuilder \
    --record-implements-suffix MyBuilder.With \
    --default-import com.example.Default \
    --ignore-import com.example.IgnoreInBuilder
```

Individual parameters override preset values when used together with
`--builder-type`.

### 2. Run Performance Measurement

Run N compilations and collect timing data. Use `--builder-type` (same as
`generate_classes.py`) to select the framework — the Maven profile and JSON
detection are handled automatically:

```bash
# SimpleBuilder — 30 runs, labeled "sb-30runs"
python3 scripts/run_performance_measurement.py \
    --runs 30 --label sb-30runs --builder-type simple-builder

# MinimalBuilder — 30 runs, labeled "mb-30runs"
python3 scripts/run_performance_measurement.py \
    --runs 30 --label mb-30runs --builder-type simple-minimal-builder

# RecordBuilder — 30 runs, wall-time only
python3 scripts/run_performance_measurement.py \
    --runs 30 --label rb-30runs --builder-type record-builder

# Lombok — 30 runs, wall-time only
python3 scripts/run_performance_measurement.py \
    --runs 30 --label lombok-30runs --builder-type lombok
```

Results are written to `target/performance-reports/<label>/`:

- `run-01.json`, `run-02.json`, ... — individual run reports (JSON profiles only)
- `summary.json` — aggregated results across all runs

#### JSON vs Wall-Time-Only Mode

The script auto-detects whether the builder type produces JSON reports:

- `simple-builder`, `simple-minimal-builder` → JSON mode (detailed metrics)
- `record-builder`, `lombok` → wall-time-only mode

#### Quick Test Run

For a quick sanity check, use a small number of runs:

```bash
python3 scripts/run_performance_measurement.py --runs 3 --label test-3runs --builder-type simple-builder
```

### 3. Compare Results

Compare two or more analysis runs side-by-side:

```bash
# Compare SimpleBuilder 3-run vs 30-run
python3 scripts/compare_performance.py \
    3runs/summary.json \
    30runs/summary.json

# Compare SimpleBuilder vs MinimalBuilder
python3 scripts/compare_performance.py \
    sb-30runs/summary.json \
    mb-30runs/summary.json

# Compare three frameworks (wall-time only for RecordBuilder and Lombok)
python3 scripts/compare_performance.py \
    sb-30runs/summary.json \
    rb-30runs/summary.json \
    lombok-30runs/summary.json
```

File paths are relative to `target/performance-reports/` (or use absolute paths).
The parent directory name is used as the column label (e.g. `sb-30runs/summary.json` → `sb-30runs`).

#### Comparison Options

| Option | Default | Description |
|---|---|---|
| `--top-classes N` | 10 | Number of slowest classes to show |
| `--top-generators N` | 5 | Number of generators to show |
| `--top-enhancers N` | 5 | Number of enhancers to show |

```bash
python3 scripts/compare_performance.py \
    --top-classes 20 --top-generators 10 \
    sb-30runs/summary.json \
    mb-30runs/summary.json
```

#### Handling Partial Data

When comparing JSON-profile runs with wall-time-only runs, the script
automatically:

- Shows **Wall Time** for all summaries (always available)
- Shows **Processor Time**, **Phase Averages**, **Top Classes**, **Generator
  Stats**, and **Enhancer Stats** only when all summaries have that data
- Displays a `Wall-time-only (no JSON)` note for summaries lacking detailed data

## Typical Workflow: Full Framework Comparison

Using a shell variable for `BUILDER_TYPE` avoids repeating the framework name:

```bash
# 1. Generate classes and measure each framework
BUILDER_TYPE=simple-builder
python3 scripts/generate_classes.py --builder-type $BUILDER_TYPE --force
python3 scripts/run_performance_measurement.py --runs 30 --label sb-30runs --builder-type $BUILDER_TYPE

BUILDER_TYPE=simple-minimal-builder
python3 scripts/generate_classes.py --builder-type $BUILDER_TYPE --force
python3 scripts/run_performance_measurement.py --runs 30 --label mb-30runs --builder-type $BUILDER_TYPE

BUILDER_TYPE=record-builder
python3 scripts/generate_classes.py --builder-type $BUILDER_TYPE --force
python3 scripts/run_performance_measurement.py --runs 30 --label rb-30runs --builder-type $BUILDER_TYPE

BUILDER_TYPE=lombok
python3 scripts/generate_classes.py --builder-type $BUILDER_TYPE --force
python3 scripts/run_performance_measurement.py --runs 30 --label lombok-30runs --builder-type $BUILDER_TYPE

# 2. Compare all four
python3 scripts/compare_performance.py \
    sb-30runs/summary.json \
    mb-30runs/summary.json \
    rb-30runs/summary.json \
    lombok-30runs/summary.json
```

## Output File Locations

```
performance-test/
├── target/
│   └── performance-reports/
│       ├── sb-30runs/
│       │   ├── run-01.json
│       │   ├── run-02.json
│       │   ├── ...
│       │   └── summary.json
│       ├── mb-30runs/
│       │   └── summary.json
│       ├── rb-30runs/
│       │   └── summary.json       (wall-time only)
│       └── lombok-30runs/
│           └── summary.json       (wall-time only)
```

## Summary JSON Structure

### JSON-Profile Summaries (simple-builder, simple-minimal-builder)

```json
{
  "runCount": 30,
  "totalClasses": 1077,
  "processorTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "averagePerClassMs": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "wallTime": { "min": ..., "max": ..., "avg": ..., "values": [...] },
  "phaseAverageNanos": { "Phase.Name": avgNanos, ... },
  "topClassesByAvg": [ { "className": ..., "avgMs": ..., ... } ],
  "generatorStats": [ { "name": ..., "avgElapsedNanos": ..., ... } ],
  "enhancerStats": [ { "name": ..., "avgElapsedNanos": ..., ... } ]
}
```

### Wall-Time-Only Summaries (record-builder, lombok)

```json
{
  "runCount": 30,
  "wallTimeOnly": true,
  "wallTime": { "min": ..., "max": ..., "avg": ..., "values": [...] }
}
```
