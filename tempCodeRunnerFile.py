#!/usr/bin/env python3
"""
Plot one CSV per data structure from benchmark_output/
Files: Treap.csv, AVLTreeMap.csv, JavaTreeMap.csv
Columns: input_size,
         random_*/sorted_*/nearly_sorted_*/reverse_* for each of:
         insert_batch_ns, insert_single_ns, search_hit_ns, search_miss_ns, inorder_ns, delete_ns

Produces one PNG per (operation x pattern), with one line per data structure.
Output: benchmark_output/figures/
"""
from __future__ import annotations

import csv
from pathlib import Path

import matplotlib.pyplot as plt

ROOT          = Path(__file__).resolve().parent
BENCHMARK_DIR = ROOT / "benchmark_output"
FIGURES_DIR   = BENCHMARK_DIR / "figures"

MAPS = ["Treap", "AVLTreeMap", "JavaTreeMap"]
COLORS = {
    "Treap":       "#2ca02c",
    "AVLTreeMap":  "#1f77b4",
    "JavaTreeMap": "#ff7f0e",
}

PATTERNS = ["random", "sorted", "nearly_sorted", "reverse"]

OPERATIONS = {
    "insert_batch":  "Insert (batch)",
    "insert_single": "Insert (single, summed)",
    "search_hit":    "Search (successful)",
    "search_miss":   "Search (unsuccessful)",
    "inorder":       "In-order traversal",
    "delete":        "Delete (all keys)",
}


def load_csv(path: Path) -> list[dict]:
    rows = []
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            rows.append({k: int(v) for k, v in row.items()})
    return rows


def col(pattern: str, op: str) -> str:
    return f"{pattern}_{op}"


def main() -> None:
    try:
        plt.style.use("seaborn-v0_8-whitegrid")
    except (OSError, ValueError):
        plt.rcParams.update({"axes.grid": True, "grid.alpha": 0.35})

    FIGURES_DIR.mkdir(parents=True, exist_ok=True)

    data: dict[str, list[dict]] = {}
    for m in MAPS:
        p = BENCHMARK_DIR / f"{m}.csv"
        if p.is_file():
            data[m] = load_csv(p)

    # one figure per (pattern x operation)
    nfig = 0
    for pattern in PATTERNS:
        for op, op_label in OPERATIONS.items():
            fig, ax = plt.subplots(figsize=(7.5, 4.5), dpi=120)
            for m, rows in data.items():
                c = col(pattern, op)
                if c not in rows[0]:
                    continue
                xs = [r["input_size"] for r in rows]
                ys = [r[c] for r in rows]
                ax.plot(xs, ys, color=COLORS.get(m, "#333333"),
                        linewidth=2, marker="o", markersize=3, label=m)
            ax.set_xlabel("n (number of keys)")
            ax.set_ylabel("Time (ns)")
            ax.set_title(f"{op_label}\nInput: {pattern.replace('_', ' ').title()}")
            ax.legend(framealpha=0.95)
            fig.tight_layout()
            fname = f"{pattern}__{op}.png"
            fig.savefig(FIGURES_DIR / fname, dpi=160, bbox_inches="tight")
            plt.close(fig)
            nfig += 1

    print(f"Wrote {nfig} figures to {FIGURES_DIR.resolve()}")


if __name__ == "__main__":
    main()