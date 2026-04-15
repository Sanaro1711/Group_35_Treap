package tree;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Compares {@link treap#treapSort}, heap sort via {@link PriorityQueue}, {@link Arrays#sort} (TimSort),
 * and {@link SortingAlgorithms} quick / merge sort. Writes CSV under {@code benchmark_output/sorting/}.
 */
public final class SortBenchmark {

    public static final int N_MIN = 100;
    public static final int N_MAX = 10_000;
    public static final int N_STEP = 100;
    public static final long BENCHMARK_RANDOM_SEED = 42L;

    public enum InputPattern {
        RANDOM,
        NEARLY_SORTED,
        REVERSE_SORTED
    }

    private static final String[] ALGORITHM_ORDER = {
            "TreapSort",
            "PQSort",
            "Java_TimSort",
            "QuickSort",
            "MergeSort",
    };

    private SortBenchmark() {
    }

    /** {@code benchmark_output/sorting} relative to project root (works if cwd is project root or {@code src}). */
    public static Path sortingOutputDir() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if ("src".equals(cwd.getFileName().toString())) {
            cwd = cwd.getParent();
        }
        return cwd.resolve("benchmark_output").resolve("sorting");
    }

    private static final class BenchCase {
        final int n;
        final InputPattern pattern;
        final Integer[] data;

        BenchCase(int n, InputPattern pattern, Integer[] data) {
            this.n = n;
            this.pattern = pattern;
            this.data = data;
        }
    }

    /** Priority-queue sort using {@link java.util.PriorityQueue} (assignment-allowed “PQ sort”). */
    public static void pqSort(Integer[] arr) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(arr.length);
        for (Integer x : arr) {
            pq.add(x);
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] = pq.poll();
        }
    }

    /** In-place sort via {@link Arrays#sort(Object[])} (TimSort for {@link Integer}[]). */
    public static void timSort(Integer[] arr) {
        Arrays.sort(arr);
    }

    public static Integer[] generateData(int n, InputPattern pattern, Random rng) {
        Integer[] a = new Integer[n];
        switch (pattern) {
            case REVERSE_SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = n - 1 - i;
                }
                break;
            case NEARLY_SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = i;
                }
                int swaps = Math.max(1, n / 50);
                for (int s = 0; s < swaps; s++) {
                    int i = rng.nextInt(n);
                    int j = rng.nextInt(n);
                    Integer t = a[i];
                    a[i] = a[j];
                    a[j] = t;
                }
                break;
            case RANDOM:
            default:
                int span = Math.max(n * 10, 1);
                for (int i = 0; i < n; i++) {
                    a[i] = rng.nextInt(span);
                }
                break;
        }
        return a;
    }

    private static void verifySorted(Integer[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1].compareTo(a[i]) > 0) {
                throw new IllegalStateException("Not sorted at index " + i);
            }
        }
    }

    private static long[] timeAndMemoryDelta(Consumer<Integer[]> sort, Integer[] arr) {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long memBefore = rt.totalMemory() - rt.freeMemory();
        long t0 = System.nanoTime();
        sort.accept(arr);
        long t1 = System.nanoTime();
        long memAfter = rt.totalMemory() - rt.freeMemory();
        verifySorted(arr);
        return new long[]{t1 - t0, memAfter - memBefore};
    }

    private static Consumer<Integer[]> sorterFor(String algo) {
        switch (algo) {
            case "TreapSort":
                return treap::treapSort;
            case "PQSort":
                return SortBenchmark::pqSort;
            case "Java_TimSort":
                return SortBenchmark::timSort;
            case "QuickSort":
                return a -> new SortingAlgorithms().quick_sort(a, 0, a.length - 1);
            case "MergeSort":
                return a -> new SortingAlgorithms().merge_sort(a, 0, a.length - 1);
            default:
                throw new IllegalArgumentException(algo);
        }
    }

    private static void writeHeader(PrintWriter out) {
        out.println("# Sorting benchmark: TreapSort, PQSort (java.util.PriorityQueue), Java TimSort (Arrays.sort),");
        out.println("# QuickSort, MergeSort (SortingAlgorithms.java).");
        out.println("# Generated local time: " + LocalDateTime.now());
        out.println("# Input sizes n: " + N_MIN + " .. " + N_MAX + " step " + N_STEP);
        out.println("# Random seed: " + BENCHMARK_RANDOM_SEED);
        out.println("# time_ns: wall-clock nanoseconds to sort one copy of the input.");
        out.println("# memory_delta_bytes: change in JVM *heap used* (after sort - before), after System.gc();");
        out.println("# Not total OS memory / RSS; very approximate and GC-dependent.");
        out.println("# Row blocks: all rows for one algorithm, then the next (same input per n+pattern across algorithms).");
        out.println("#");
        out.println("n,input_pattern,algorithm,time_ns,memory_delta_bytes");
    }

    public static void main(String[] args) throws Exception {
        Random rng = new Random(BENCHMARK_RANDOM_SEED);
        List<BenchCase> cases = new ArrayList<>();
        for (int n = N_MIN; n <= N_MAX; n += N_STEP) {
            for (InputPattern p : InputPattern.values()) {
                cases.add(new BenchCase(n, p, generateData(n, p, rng)));
            }
        }

        Path outDir = sortingOutputDir();
        Files.createDirectories(outDir);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        Path csv = outDir.resolve("sort_benchmark_" + stamp + ".csv");

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(csv, StandardCharsets.UTF_8))) {
            writeHeader(out);
            for (String algo : ALGORITHM_ORDER) {
                out.println("# --- block: " + algo + " ---");
                Consumer<Integer[]> sort = sorterFor(algo);
                for (BenchCase c : cases) {
                    Integer[] copy = Arrays.copyOf(c.data, c.data.length);
                    long[] tm = timeAndMemoryDelta(sort, copy);
                    out.printf("%d,%s,%s,%d,%d%n", c.n, c.pattern.name(), algo, tm[0], tm[1]);
                }
            }
        }

        System.out.println("Sort benchmark CSV: " + csv.toAbsolutePath().normalize());
    }
}
