import csv
from pathlib import Path
import matplotlib.pyplot as plt

BENCHMARK_DIR = Path("src/benchmark_output")
FIGURES_DIR   = Path("src/benchmark_output/figures")

MAPS = ["Treap", "AVLTreeMap", "JavaTreeMap"]
COLORS = {
    "Treap":       "#2ca02c",
    "AVLTreeMap":  "#1f77b4",
    "JavaTreeMap": "#ff7f0e",
}

PATTERNS = ["random", "sorted", "nearly_sorted", "reverse"]

# operation suffix in CSV -> readable label
OPERATIONS = {
    "insert_batch":  "Insert (batch)",
    "insert_single": "Insert (single)",
    "search_hit":    "Search (hit)",
    "search_miss":   "Search (miss)",
    "inorder":       "In-order traversal",
    "delete":        "Delete",
}


def load(path):
    rows = []
    with open(path, newline="") as f:
        for row in csv.DictReader(f):
            if None in row.values():
                continue
            rows.append({k: int(v) for k, v in row.items()})
    return rows


def main():
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)

    data = {}
    for m in MAPS:
        p = BENCHMARK_DIR / f"{m}.csv"
        if p.is_file():
            data[m] = load(p)

    for m, rows in data.items():
        print(f"{m}: {len(rows)} rows, columns: {list(rows[0].keys())}")

    try:
        plt.style.use("seaborn-v0_8-whitegrid")
    except Exception:
        plt.rcParams.update({"axes.grid": True, "grid.alpha": 0.35})

    nfig = 0
    for pattern in PATTERNS:
        for op_suffix, op_label in OPERATIONS.items():
            col_name = f"{pattern}_{op_suffix}"

            fig, ax = plt.subplots(figsize=(8, 5))
            for m, rows in data.items():
                if col_name not in rows[0]:
                    continue
                xs = [r["input_size"] for r in rows]
                ys = [r[col_name] for r in rows]
                ax.plot(xs, ys, color=COLORS[m], linewidth=2, marker="o", markersize=3, label=m)
            ax.set_xlabel("Input size (n)")
            ax.set_ylabel("Time (ns)")
            ax.set_title(f"{op_label} — {pattern.replace('_', ' ').title()}")
            ax.legend()
            fig.tight_layout()
            fig.savefig(FIGURES_DIR / f"{pattern}_{op_suffix}.png", dpi=150)
            plt.close(fig)
            nfig += 1

    print(f"Saved {nfig} figures to {FIGURES_DIR}")


if __name__ == "__main__":
    main()