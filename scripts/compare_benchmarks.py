#!/usr/bin/env python3
"""
Generate a Markdown performance comparison table from JMH benchmark results.

Usage:
  python scripts/compare_benchmarks.py                # latest file in jmh/benchmark-results/
  python scripts/compare_benchmarks.py results.json   # specific file by name or full path
  python scripts/compare_benchmarks.py a.json b.json  # fastcollect raw scores from two runs

Output is Markdown, redirect to file for inclusion in project docs:
  python scripts/compare_benchmarks.py > docs/PERFORMANCE_BENCHMARKS.md
"""

import json
import sys
from pathlib import Path

IMPLS = ['fastcollect', 'fastutil', 'hashsmith', 'eclipse', 'androidx', 'kotlin']
BENCH_TYPES = ['List', 'Map', 'Set']


# -- Path helpers --------------------------------------------------------------

def find_results_dir() -> Path:
    for candidate in (Path('jmh/benchmark-results'), Path('../jmh/benchmark-results')):
        if candidate.is_dir():
            return candidate
    raise FileNotFoundError(
        "Cannot find jmh/benchmark-results/. Run this script from the project root."
    )


def find_latest() -> Path:
    d = find_results_dir()
    files = sorted(d.glob('*.json'))
    if not files:
        raise FileNotFoundError(f"No .json files found in {d}")
    return files[-1]


def resolve_path(arg: str) -> Path:
    p = Path(arg)
    if p.exists():
        return p
    q = find_results_dir() / arg
    if q.exists():
        return q
    raise FileNotFoundError(f"Cannot find benchmark file: {arg}")


# -- Loading -------------------------------------------------------------------

def parse_method(method: str):
    """Split 'fastcollectGetHit' into ('fastcollect', 'GetHit')."""
    for impl in IMPLS:
        if method.startswith(impl):
            return impl, method[len(impl):]
    return None, None


def load(path: Path) -> dict:
    """
    Returns: {(bench_type, op, size): {impl: (score, unit)}}
    Mode (avgt vs ss) is intentionally discarded — all operations are grouped
    purely by collection type, operation name, and size.
    """
    with open(path, encoding='utf-8') as f:
        data = json.load(f)

    results = {}
    for entry in data:
        parts = entry['benchmark'].split('.')
        bench_type = parts[-2].replace('Benchmark', '')
        impl, op = parse_method(parts[-1])
        if impl is None:
            continue
        size = int(entry['params']['size'])
        pm = entry['primaryMetric']
        key = (bench_type, op, size)
        results.setdefault(key, {})[impl] = (pm['score'], pm['scoreUnit'])

    return results


def ops_for(results: dict, bench_type: str) -> list:
    return sorted({op for (bt, op, _s) in results if bt == bench_type})


def sizes_for(results: dict, bench_type: str, op: str) -> list:
    return sorted(s for (bt, o, s) in results if bt == bench_type and o == op)


def unit_for(results: dict, bench_type: str, op: str) -> str:
    for (bt, o, _s), impls in results.items():
        if bt == bench_type and o == op:
            return next(iter(impls.values()))[1]
    return '?'


UNIT_SUFFIX = {'ns/op': 'ns', 'us/op': 'us', 'ms/op': 'ms'}


def to_ns(score: float, unit: str) -> float:
    return score * {'ns/op': 1.0, 'us/op': 1e3, 'ms/op': 1e6}.get(unit, 1.0)


def fmt(score: float, unit: str) -> str:
    return f"`{score:.3f} {UNIT_SUFFIX.get(unit, unit)}`"


# -- Single-run Markdown -------------------------------------------------------

def render(results: dict, source: str) -> None:
    print(f"# Performance Benchmarks")
    print()
    print(f"Source: `{source}`")
    print()
    print("All times are lower-is-better.")
    print()

    for bt in BENCH_TYPES:
        ops = ops_for(results, bt)
        if not ops:
            continue

        print(f"## {bt}")
        print()

        for op in ops:
            sizes = sizes_for(results, bt, op)
            impls_present = set()
            for size in sizes:
                impls_present.update(results.get((bt, op, size), {}).keys())
            impls = [i for i in IMPLS if i in impls_present]
            if not impls or not sizes:
                continue

            unit = unit_for(results, bt, op)
            print(f"### {op} ({unit})")
            print()

            # Header
            size_cols = " | ".join(f"{s:,}" for s in sizes)
            print(f"| Library / Size | {size_cols} |")
            print(f"|:--- |" + " ---: |" * len(sizes))

            # Data rows
            for impl in impls:
                cells = []
                for size in sizes:
                    entry = results.get((bt, op, size), {}).get(impl)
                    cells.append(fmt(entry[0], entry[1]) if entry else "`—`")
                print(f"| `{impl}` | " + " | ".join(cells) + " |")

            print()


# -- Two-run fastcollect comparison --------------------------------------------

def render_comparison(old: dict, new: dict, old_label: str, new_label: str) -> None:
    """
    Renders fastcollect raw scores from two runs side by side, with sizes as
    columns labeled by run. Useful for tracking regressions or improvements.
    """
    print(f"# fastcollect: {old_label} vs {new_label}")
    print()
    print("Raw scores for fastcollect only. All times are lower-is-better.")
    print()

    all_keys = set(old) | set(new)
    BASELINE = 'fastcollect'

    for bt in BENCH_TYPES:
        ops = sorted({op for (b, op, _s) in all_keys if b == bt})
        if not ops:
            continue

        print(f"## {bt}")
        print()

        sizes = sorted({s for (b, _o, s) in all_keys if b == bt})
        # Two columns per size: old and new
        header_parts = []
        for s in sizes:
            header_parts.append(f"{s:,} ({old_label})")
            header_parts.append(f"{s:,} ({new_label})")

        print(f"| Operation | " + " | ".join(header_parts) + " |")
        print(f"|:--- |" + " ---: |" * len(header_parts))

        for op in ops:
            unit = unit_for(old, bt, op) or unit_for(new, bt, op)
            cells = []
            for size in sizes:
                old_e = old.get((bt, op, size), {}).get(BASELINE)
                new_e = new.get((bt, op, size), {}).get(BASELINE)
                cells.append(fmt(old_e[0], old_e[1]) if old_e else "`—`")
                cells.append(fmt(new_e[0], new_e[1]) if new_e else "`—`")
            print(f"| `{op} ({unit})` | " + " | ".join(cells) + " |")

        print()


# -- Main ----------------------------------------------------------------------

def main() -> None:
    args = sys.argv[1:]

    if len(args) == 0:
        path = find_latest()
        render(load(path), path.name)

    elif len(args) == 1:
        path = resolve_path(args[0])
        render(load(path), path.name)

    elif len(args) == 2:
        old_path = resolve_path(args[0])
        new_path = resolve_path(args[1])
        render_comparison(
            load(old_path), load(new_path),
            old_path.stem, new_path.stem,
        )

    else:
        print(__doc__, file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
