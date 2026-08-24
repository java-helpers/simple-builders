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

## Typical Workflow

Always regenerate classes when switching builder types (use `--force` to
overwrite). Use `--dry-run` to preview without writing.

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
