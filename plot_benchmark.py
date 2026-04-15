#!/usr/bin/env python3
"""
Read tree_map_benchmark_*.csv from benchmark_output/ and save line plots:
  one PNG per (metric × input_pattern), three lines (Treap, AVL, Java TreeMap).

Usage:
  python plot_benchmark.py
  python plot_benchmark.py benchmark_output/tree_map_benchmark_2026-04-15_232243.csv
Requires: pip install matplotlib
"""
from __future__ import annotations

import csv
import sys
from pathlib import Path

import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
BENCHMARK_DIR = ROOT / "benchmark_output"
OUT_DIR = BENCHMARK_DIR / "figures"

MAP_ORDER = ["treap", "AVLTreeMap", "java.util.TreeMap"]
MAP_LABEL = {
    "treap": "Treap",
    "AVLTreeMap": "AVL",
    "java.util.TreeMap": "Java TreeMap",
}
COLORS = {
    "treap": "#2ca02c",
    "AVLTreeMap": "#1f77b4",
    "java.util.TreeMap": "#ff7f0e",
}

METRICS = [
    ("insert_batch_ns", "Insert (batch)", "Time (ns)"),
    ("insert_single_calls_sum_ns", "Insert (single, summed)", "Time (ns)"),
    ("search_successful_ns", "Search (successful)", "Time (ns)"),
    ("search_unsuccessful_ns", "Search (unsuccessful)", "Time (ns)"),
    ("inorder_traversal_ns", "In-order traversal", "Time (ns)"),
    ("delete_all_ns", "Delete (all keys)", "Time (ns)"),
]

PATTERNS = ["RANDOM", "ASCENDING", "DESCENDING", "PARTIALLY_SORTED"]


def find_csv(arg: str | None) -> Path:
    if arg:
        p = Path(arg)
        if not p.is_absolute():
            p = ROOT / p
        if not p.is_file():
            sys.exit(f"File not found: {p}")
        return p
    files = sorted(
        BENCHMARK_DIR.glob("tree_map_benchmark_*.csv"),
        key=lambda x: x.stat().st_mtime,
    )
    if not files:
        sys.exit(f"No tree_map_benchmark_*.csv under {BENCHMARK_DIR}")
    return files[-1]


def load_rows(path: Path) -> list[dict]:
    header: list[str] | None = None
    rows: list[dict] = []
    with path.open(newline="", encoding="utf-8") as f:
        for row in csv.reader(f):
            if not row or not row[0] or row[0].startswith("#"):
                continue
            if row[0] == "size_n":
                header = row
                continue
            if header is None or len(row) != len(header):
                continue
            rows.append(dict(zip(header, row)))
    numeric = [
        "size_n",
        "insert_batch_ns",
        "insert_single_calls_sum_ns",
        "search_successful_ns",
        "search_unsuccessful_ns",
        "inorder_traversal_ns",
        "delete_all_ns",
    ]
    for r in rows:
        for k in numeric:
            r[k] = int(r[k])
    return rows


def pattern_title(p: str) -> str:
    return p.replace("_", " ").title()


def main() -> None:
    csv_path = find_csv(sys.argv[1] if len(sys.argv) > 1 else None)
    rows = load_rows(csv_path)
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    try:
        plt.style.use("seaborn-v0_8-whitegrid")
    except (OSError, ValueError):
        try:
            plt.style.use("seaborn-whitegrid")
        except (OSError, ValueError):
            plt.rcParams.update({"axes.grid": True, "grid.alpha": 0.35})

    saved = []
    for pattern in PATTERNS:
        for key, title, ylab in METRICS:
            fig, ax = plt.subplots(figsize=(7.5, 4.5), dpi=120)
            for impl in MAP_ORDER:
                pts = [
                    r
                    for r in rows
                    if r["input_pattern"] == pattern and r["map_implementation"] == impl
                ]
                pts.sort(key=lambda r: r["size_n"])
                if not pts:
                    continue
                xs = [r["size_n"] for r in pts]
                ys = [r[key] for r in pts]
                ax.plot(
                    xs,
                    ys,
                    color=COLORS[impl],
                    linewidth=2,
                    marker="o",
                    markersize=3,
                    label=MAP_LABEL[impl],
                )
            ax.set_xlabel("n (number of keys)")
            ax.set_ylabel(ylab)
            ax.set_title(f"{title}\nInput: {pattern_title(pattern)}")
            ax.legend(framealpha=0.95)
            fig.tight_layout()
            fname = f"{pattern.lower()}__{key}.png"
            fig.savefig(OUT_DIR / fname, dpi=160, bbox_inches="tight")
            plt.close(fig)
            saved.append(fname)

    print(f"CSV: {csv_path}")
    print(f"Wrote {len(saved)} figures to {OUT_DIR.resolve()}")


if __name__ == "__main__":
    main()
