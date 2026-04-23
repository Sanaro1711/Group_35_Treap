#!/usr/bin/env python3
"""
Plot sort_benchmark_*.csv → benchmark_output/sorting/figures/

- random__time_ms.png, reverse_sorted__time_ms.png
- random__memory_delta_bytes.png (single memory chart; RANDOM only; -1 rows ignored)

Usage:
  python plot_sort_benchmark.py [path/to/sort_benchmark_....csv]
Requires: pip install matplotlib
"""
from __future__ import annotations

import csv
import sys
from pathlib import Path

import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
SORTING_DIR = ROOT / "benchmark_output" / "sorting"
FIGURES_DIR = SORTING_DIR / "figures"

ALGOS = [
    "TreapSort",
    "PQSort",
    "Java_TimSort",
    "QuickSort",
    "MergeSort",
]
COLORS = {
    "TreapSort": "#2ca02c",
    "PQSort": "#9467bd",
    "Java_TimSort": "#ff7f0e",
    "QuickSort": "#1f77b4",
    "MergeSort": "#d62728",
}
TIME_PATTERNS = ["RANDOM", "REVERSE_SORTED"]


def find_csv(arg: str | None) -> Path:
    if arg:
        p = Path(arg)
        if not p.is_absolute():
            p = ROOT / p
        if not p.is_file():
            sys.exit(f"File not found: {p}")
        return p
    files = sorted(
        SORTING_DIR.glob("sort_benchmark_*.csv"),
        key=lambda x: x.stat().st_mtime,
    )
    if not files:
        sys.exit(f"No sort_benchmark_*.csv under {SORTING_DIR}")
    return files[-1]


def load_rows(path: Path) -> list[dict]:
    rows: list[dict] = []
    header: list[str] | None = None
    with path.open(newline="", encoding="utf-8") as f:
        for row in csv.reader(f):
            if not row or not row[0] or row[0].startswith("#"):
                continue
            if row[0] == "n":
                header = row
                continue
            if header is None or len(row) != len(header):
                continue
            rows.append(dict(zip(header, row)))
    for r in rows:
        r["n"] = int(r["n"])
        r["time_ns"] = int(r["time_ns"])
        r["time_ms"] = r["time_ns"] / 1_000_000.0
        md = int(r["memory_delta_bytes"])
        r["memory_delta_bytes"] = md if md >= 0 else None
    return rows


def pattern_title(p: str) -> str:
    return p.replace("_", " ").title()


def plot_lines(ax, rows: list[dict], pattern: str, metric_key: str) -> None:
    for algo in ALGOS:
        pts = [r for r in rows if r["input_pattern"] == pattern and r["algorithm"] == algo]
        pts.sort(key=lambda r: r["n"])
        if not pts:
            continue
        xs = [r["n"] for r in pts]
        ys = [r[metric_key] for r in pts]
        ax.plot(
            xs,
            ys,
            color=COLORS.get(algo, "#333333"),
            linewidth=2,
            marker="o",
            markersize=3,
            label=algo,
        )


def plot_metric(rows: list[dict], pattern: str, metric_key: str, y_label: str, title: str, fname: str) -> None:
    fig, ax = plt.subplots(figsize=(7.5, 4.5), dpi=120)
    plot_lines(ax, rows, pattern, metric_key)
    ax.set_xlabel("n (array length)")
    ax.set_ylabel(y_label)
    ax.set_title(title)
    ax.legend(framealpha=0.95, fontsize=8)
    fig.tight_layout()
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    fig.savefig(FIGURES_DIR / fname, dpi=160, bbox_inches="tight")
    plt.close(fig)


def plot_memory_random_only(rows: list[dict]) -> None:
    """One chart: RANDOM inputs only, rows with memory measured."""
    fig, ax = plt.subplots(figsize=(7.5, 4.5), dpi=120)
    for algo in ALGOS:
        pts = [
            r
            for r in rows
            if r["input_pattern"] == "RANDOM"
            and r["algorithm"] == algo
            and r["memory_delta_bytes"] is not None
        ]
        pts.sort(key=lambda r: r["n"])
        if not pts:
            continue
        xs = [r["n"] for r in pts]
        ys = [r["memory_delta_bytes"] for r in pts]
        ax.plot(
            xs,
            ys,
            color=COLORS.get(algo, "#333333"),
            linewidth=2,
            marker="o",
            markersize=3,
            label=algo,
        )
    ax.set_xlabel("n (array length)")
    ax.set_ylabel("Δ JVM heap used (bytes)")
    ax.set_title(
        "Random input — approximate heap change\n(after gc post-sort minus before gc pre-sort)"
    )
    ax.legend(framealpha=0.95, fontsize=8)
    fig.tight_layout()
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    fig.savefig(FIGURES_DIR / "random__memory_delta_bytes.png", dpi=160, bbox_inches="tight")
    plt.close(fig)


def main() -> None:
    csv_path = find_csv(sys.argv[1] if len(sys.argv) > 1 else None)
    rows = load_rows(csv_path)

    try:
        plt.style.use("seaborn-v0_8-whitegrid")
    except (OSError, ValueError):
        try:
            plt.style.use("seaborn-whitegrid")
        except (OSError, ValueError):
            plt.rcParams.update({"axes.grid": True, "grid.alpha": 0.35})

    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    for old in FIGURES_DIR.glob("*.png"):
        old.unlink()

    nfig = 0
    for pattern in TIME_PATTERNS:
        plot_metric(
            rows,
            pattern,
            "time_ms",
            "Time (ms)",
            f"Sort time — {pattern_title(pattern)} input",
            f"{pattern.lower()}__time_ms.png",
        )
        nfig += 1

    plot_memory_random_only(rows)
    nfig += 1

    print(f"CSV: {csv_path}")
    print(f"Wrote {nfig} figures to {FIGURES_DIR.resolve()}")


if __name__ == "__main__":
    main()
