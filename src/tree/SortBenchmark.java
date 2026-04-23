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
 * Compares {@link treap#treapSort}, {@link PriorityQueue} sort, {@link Arrays#sort} (TimSort),
 * and {@link SortingAlgorithms} quick / merge sort. Writes CSV under {@code benchmark_output/sorting/}.
 */
public final class SortBenchmark {

    public static final int N_MIN = 100;
    public static final int N_MAX = 10_000;
    public static final int N_STEP = 100;
    public static final long BENCHMARK_RANDOM_SEED = 42L;

    /** Only random and reverse-sorted inputs (no nearly-sorted). */
    public enum InputPattern {
        RANDOM,
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

    public static void pqSort(Integer[] arr) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(arr.length); // we used Java's in-built PQ
        for (Integer x : arr) {
            pq.add(x);
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] = pq.poll();
        }
    }

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

    private static long usedHeapAfterGc() {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        return rt.totalMemory() - rt.freeMemory();
    }

    /** Time + heap delta (for RANDOM inputs only in the CSV). */
    private static long[] timeAndMemoryDelta(Consumer<Integer[]> sort, Integer[] arr) {
        long memBefore = usedHeapAfterGc();
        long t0 = System.nanoTime();
        sort.accept(arr);
        long t1 = System.nanoTime();
        verifySorted(arr);
        long memAfter = usedHeapAfterGc();
        return new long[]{t1 - t0, memAfter - memBefore};
    }

    /** Time only (no extra gc); used for reverse-sorted rows. */
    private static long timeNanosOnly(Consumer<Integer[]> sort, Integer[] arr) {
        long t0 = System.nanoTime();
        sort.accept(arr);
        long t1 = System.nanoTime();
        verifySorted(arr);
        return t1 - t0;
    }

    private static void warmupSorters() {
        Random w = new Random(999L);
        Integer[] warm = generateData(2_000, InputPattern.RANDOM, w);
        for (String algo : ALGORITHM_ORDER) {
            Integer[] c = Arrays.copyOf(warm, warm.length);
            sorterFor(algo).accept(c);
            verifySorted(c);
        }
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
        out.println("# Sorting benchmark: TreapSort, PQSort, Java TimSort, QuickSort, MergeSort.");
        out.println("# Input patterns: RANDOM, REVERSE_SORTED only.");
        out.println("# Generated local time: " + LocalDateTime.now());
        out.println("# n: " + N_MIN + " .. " + N_MAX + " step " + N_STEP + ", seed " + BENCHMARK_RANDOM_SEED);
        out.println("# JIT warmup: each sorter on random n=2000 before timed runs.");
        out.println("# time_ns: wall-clock time to sort one array copy.");
        out.println("# memory_delta_bytes: only for RANDOM (heap after gc post-sort minus before); -1 = not measured.");
        out.println("#");
        out.println("n,input_pattern,algorithm,time_ns,memory_delta_bytes");
    }

    public static void main(String[] args) throws Exception {
        warmupSorters();

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
                    if (c.pattern == InputPattern.RANDOM) {
                        long[] tm = timeAndMemoryDelta(sort, copy);
                        out.printf("%d,%s,%s,%d,%d%n", c.n, c.pattern.name(), algo, tm[0], tm[1]);
                    } else {
                        long t = timeNanosOnly(sort, copy);
                        out.printf("%d,%s,%s,%d,-1%n", c.n, c.pattern.name(), algo, t);
                    }
                }
            }
        }

        System.out.println("Sort benchmark CSV: " + csv.toAbsolutePath().normalize());
    }
}
