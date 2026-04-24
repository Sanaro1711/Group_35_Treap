import csv
from pathlib import Path
import matplotlib.pyplot as plt
import numpy as np

SORTING_DIR = Path("src/benchmark_output/sorting")
FIGURES_DIR = Path("src/benchmark_output/sorting/figures")

ALGOS = ["TreapSort", "PQSort", "Java_TimSort", "QuickSort", "MergeSort"]
COLORS = {
    "TreapSort":    "#2ca02c",
    "PQSort":       "#9467bd",
    "Java_TimSort": "#ff7f0e",
    "QuickSort":    "#1f77b4",
    "MergeSort":    "#d62728",
}

TIME_COLS = {
    "Random":        "random_time",
    "Sorted":        "sorted_time",
    "Nearly Sorted": "nearly_sorted_time",
    "Reverse Sorted":"reverse_sorted_time",
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
    for algo in ALGOS:
        p = SORTING_DIR / f"{algo}.csv"
        if p.is_file():
            data[algo] = load(p)

    try:
        plt.style.use("seaborn-v0_8-whitegrid")
    except Exception:
        plt.rcParams.update({"axes.grid": True, "grid.alpha": 0.35})

    for pattern_name, col_name in TIME_COLS.items():
        fig, ax = plt.subplots(figsize=(8, 5))

        all_ys = []
        for algo, rows in data.items():
            xs = [r["input_size"] for r in rows]
            ys = [r[col_name] / 1_000_000 for r in rows]
            all_ys.extend([r[col_name] / 1_000_000 for r in rows])
            ax.plot(xs, ys, color=COLORS[algo], linewidth=2, marker="o", markersize=3, label=algo)
        ax.set_xlabel("Input size (n)")
        ax.set_ylabel("Time (ms)")
        ax.set_title(f"Sort time — {pattern_name}")
        ax.set_ylim(0, np.percentile(all_ys, 100))#this is the percentage of values along the y axis that are show on the graph useful for being able to see the differences
        #between the nlon algorithms even if quicksort has n squared complexity so everything else just looks like 1 line otherwise
        ax.legend(fontsize=8)
        fig.tight_layout()
        fig.savefig(FIGURES_DIR / f"{pattern_name.lower().replace(' ', '_')}_time.png", dpi=150)
        plt.close(fig)
        print(f"Saved: {pattern_name} time")

    fig, ax = plt.subplots(figsize=(8, 5))
    for algo, rows in data.items():
        xs = [r["input_size"] for r in rows]
        ys = [max(0, r["random_memory_bytes"]) for r in rows]
        ax.plot(xs, ys, color=COLORS[algo], linewidth=2, marker="o", markersize=3, label=algo)
    ax.set_xlabel("Input size (n)")
    ax.set_ylabel("Memory delta (bytes)")
    ax.set_title("Memory usage — Random input")
    ax.legend(fontsize=8)
    fig.tight_layout()
    fig.savefig(FIGURES_DIR / "random_memory.png", dpi=150)
    plt.close(fig)
    print("Saved: random memory")


if __name__ == "__main__":
    main()