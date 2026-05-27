#!/usr/bin/env python3
"""
Convert a benchmark CSV (produced by parse_results.py) into a Markdown file.

One table is emitted per benchmark.  Sizes become columns, libraries become
rows, so all sizes for a given library/order are visible in one row.
The unit is shown in the table heading.

Usage:
    python jmh/results_to_markdown.py                        # latest CSV in benchmark-results/
    python jmh/results_to_markdown.py path/to/results.csv    # explicit CSV input
    python jmh/results_to_markdown.py results.csv --out results.md
"""

import argparse
import csv
import math
from pathlib import Path


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def find_latest_csv(results_dir: Path) -> Path:
    files = sorted(results_dir.glob("*.csv"))
    if not files:
        raise FileNotFoundError(f"No CSV files found in {results_dir}")
    return files[-1]


def fmt_score(value: float) -> str:
    """Format a score to 3 decimal places, never using scientific notation."""
    return f"{value:.3f}"


def geomean(values: list[float]) -> float:
    return math.exp(sum(math.log(v) for v in values) / len(values))


def md_row(cells: list[str]) -> str:
    return "| " + " | ".join(cells) + " |"


def md_separator(n_label_cols: int, n_data_cols: int) -> str:
    return "| " + " | ".join(["---"] * n_label_cols + ["---:"] * n_data_cols) + " |"


# ---------------------------------------------------------------------------
# Core
# ---------------------------------------------------------------------------

def load_csv(csv_path: Path) -> list[dict]:
    with csv_path.open(encoding="utf-8") as f:
        return list(csv.DictReader(f))


def build_markdown(rows: list[dict]) -> str:
    has_order = "order" in rows[0]

    # Derive stable ordered lists of dimension values from the data as-encountered.
    benchmarks = list(dict.fromkeys(r["benchmark"] for r in rows))
    libraries  = list(dict.fromkeys(r["library"]   for r in rows))

    sections: list[str] = []

    for benchmark in benchmarks:
        bench_rows = [r for r in rows if r["benchmark"] == benchmark]
        unit = bench_rows[0]["unit"]

        sections.append(f"## {benchmark} ({unit})\n")

        # Build a lookup: (order, library) -> {size -> score}
        scores: dict[tuple, dict[int, str]] = {}
        for r in bench_rows:
            key = (r.get("order", ""), r["library"])
            scores.setdefault(key, {})[int(r["size"])] = r["score"]

        sizes = sorted({int(r["size"]) for r in bench_rows})

        # Row keys sorted: order reverse-alpha (stable over library appearance order)
        row_keys = list(dict.fromkeys((r.get("order", ""), r["library"]) for r in bench_rows))
        row_keys.sort(key=lambda k: k[0], reverse=True)            # order reverse-alpha (stable)

        # Column headers: (order,) library, size1, size2, …
        header_dims = (["order"] if has_order else []) + ["library"]
        header = md_row(header_dims + [f"{s:,}" for s in sizes])
        sep    = md_separator(len(header_dims), len(sizes))

        table_lines = [header, sep]
        for (order, library) in row_keys:
            lib_scores = scores[(order, library)]
            dim_cells   = (([order] if has_order else []) + [library])
            score_cells = [fmt_score(float(lib_scores[s])) if s in lib_scores else "" for s in sizes]
            table_lines.append(md_row(dim_cells + score_cells))

        sections.append("\n".join(table_lines))
        sections.append("")

        # Summary table: geomean across (orders × sizes) per library
        if has_order:
            all_orders = list(dict.fromkeys(r.get("order", "") for r in bench_rows))
            summary_header = md_row(["library", "seq + random", "all"])
            summary_sep    = md_separator(1, 2)
            summary_lines  = [summary_header, summary_sep]

            for library in libraries:
                row_cells = []
                for order_filter in [{"sequential", "random"}, set(all_orders)]:
                    vals = [
                        float(scores[(order, library)][size])
                        for order in order_filter
                        for size in sizes
                        if (order, library) in scores and size in scores[(order, library)]
                    ]
                    row_cells.append(fmt_score(geomean(vals)) if vals else "")
                summary_lines.append(md_row([library] + row_cells))

            sections.append("\n".join(summary_lines))
            sections.append("")

    return "\n".join(sections)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    script_dir  = Path(__file__).parent
    results_dir = script_dir / "benchmark-results"

    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "file", type=Path, nargs="?", default=None,
        help="CSV to convert (default: latest file in benchmark-results/)",
    )
    parser.add_argument(
        "--out", type=Path, default=None,
        help="output Markdown path (default: <csv-stem>.md next to the CSV)",
    )
    args = parser.parse_args()

    csv_path = args.file if args.file else find_latest_csv(results_dir)
    out_path = args.out  if args.out  else csv_path.with_suffix(".md")

    print(f"Input : {csv_path}")
    rows = load_csv(csv_path)
    md   = build_markdown(rows)
    out_path.write_text(md, encoding="utf-8")
    print(f"Output: {out_path}")


if __name__ == "__main__":
    main()
