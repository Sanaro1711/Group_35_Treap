package tree;

import interfaces.Entry;
import interfaces.Map;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Simple timing helpers (nanoseconds). Build a map, then call the measure methods you need.
 */
public final class MeasurePerformance {

    public static final int N_MIN = 100;
    public static final int N_MAX = 10_000;
    public static final int N_STEP = 100;

    /** Used for {@link #makeKeys} / shuffles so runs are reproducible. */
    public static final long BENCHMARK_RANDOM_SEED = 42L;

    /** Default folder (under the JVM working directory) for benchmark CSV output. */
    public static final String DEFAULT_OUTPUT_DIR = "benchmark_output";

    public enum InputPattern {
        RANDOM,
        ASCENDING,
        DESCENDING,
        PARTIALLY_SORTED
    }

    private static volatile long blackhole;

    private MeasurePerformance() {
    }

    // ----- project maps (interfaces.Map) -----

    public static long measureInsert(Map<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.put(key, key);
        }
        return System.nanoTime() - start;
    }

    /** One timer per put; returns total nanoseconds (rough “single insert” cost summed). */
    public static long measureInsertSingleCalls(Map<Integer, Integer> map, List<Integer> keys) {
        long total = 0;
        for (Integer key : keys) {
            long t0 = System.nanoTime();
            map.put(key, key);
            total += System.nanoTime() - t0;
        }
        return total;
    }

    public static long measureSearch(Map<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.get(key);
        }
        return System.nanoTime() - start;
    }

    public static long measureDelete(Map<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.remove(key);
        }
        return System.nanoTime() - start;
    }

    public static long measureInorder(Map<Integer, Integer> map) {
        long acc = 0;
        long start = System.nanoTime();
        for (Entry<Integer, Integer> e : map.entrySet()) {
            acc += e.getKey() + e.getValue();
        }
        blackhole = acc;
        return System.nanoTime() - start;
    }

    // ----- java.util.TreeMap -----

    public static long measureInsert(TreeMap<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.put(key, key);
        }
        return System.nanoTime() - start;
    }

    public static long measureInsertSingleCalls(TreeMap<Integer, Integer> map, List<Integer> keys) {
        long total = 0;
        for (Integer key : keys) {
            long t0 = System.nanoTime();
            map.put(key, key);
            total += System.nanoTime() - t0;
        }
        return total;
    }

    public static long measureSearch(TreeMap<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.get(key);
        }
        return System.nanoTime() - start;
    }

    public static long measureDelete(TreeMap<Integer, Integer> map, List<Integer> keys) {
        long start = System.nanoTime();
        for (Integer key : keys) {
            map.remove(key);
        }
        return System.nanoTime() - start;
    }

    public static long measureInorder(TreeMap<Integer, Integer> map) {
        long acc = 0;
        long start = System.nanoTime();
        for (var e : map.entrySet()) {
            acc += e.getKey() + e.getValue();
        }
        blackhole = acc;
        return System.nanoTime() - start;
    }

    // ----- test data -----

    public static List<Integer> makeKeys(int n, InputPattern pattern, Random rng) {
        List<Integer> keys = new ArrayList<>(n);
        switch (pattern) {
            case ASCENDING:
                for (int i = 0; i < n; i++) {
                    keys.add(i);
                }
                break;
            case DESCENDING:
                for (int i = 0; i < n; i++) {
                    keys.add(n - 1 - i);
                }
                break;
            case PARTIALLY_SORTED:
                for (int i = 0; i < n; i++) {
                    keys.add(i);
                }
                for (int s = 0, swaps = Math.max(1, n / 10); s < swaps; s++) {
                    swap(keys, rng.nextInt(n), rng.nextInt(n));
                }
                break;
            case RANDOM:
            default:
                HashSet<Integer> seen = new HashSet<>(n * 2);
                int span = Math.max(n * 10, 1);
                while (keys.size() < n) {
                    int v = rng.nextInt(span);
                    if (seen.add(v)) {
                        keys.add(v);
                    }
                }
                break;
        }
        return keys;
    }

    public static List<Integer> absentKeys(int n) {
        List<Integer> m = new ArrayList<>(n);
        int base = n * 10_000;
        for (int i = 0; i < n; i++) {
            m.add(base + i);
        }
        return m;
    }

    public static List<Integer> shuffledCopy(List<Integer> keys, Random rng) {
        List<Integer> c = new ArrayList<>(keys);
        for (int i = c.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            swap(c, i, j);
        }
        return c;
    }

    private static void swap(List<Integer> list, int i, int j) {
        Integer t = list.get(i);
        list.set(i, list.get(j));
        list.set(j, t);
    }

    public static double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    /**
     * Runs the full benchmark and writes a comma-separated file with a comment header
     * describing columns and parameters.
     *
     * @param args optional: output CSV path; if omitted, writes under {@value #DEFAULT_OUTPUT_DIR}
     *             as {@code tree_map_benchmark_<timestamp>.csv} (relative to the working directory).
     */
    public static void main(String[] args) throws Exception {
        Path csvPath = resolveOutputPath(args);
        Path parent = csvPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Random rng = new Random(BENCHMARK_RANDOM_SEED);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8))) {
            writeCsvHeader(out);
            runBenchmarkRows(out, rng);
            out.println("# jit_blackhole_accumulator_do_not_use," + blackhole);
        }

        System.out.println("Benchmark CSV written to: " + csvPath.toAbsolutePath().normalize());
    }

    private static Path resolveOutputPath(String[] args) {
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return Path.of(args[0].trim());
        }
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        return Path.of(DEFAULT_OUTPUT_DIR, "tree_map_benchmark_" + stamp + ".csv");
    }

    private static void writeCsvHeader(PrintWriter out) {
        out.println("# Tree map performance comparison (course benchmark)");
        out.println("# Maps compared: treap (this project), AVLTreeMap (this project), java.util.TreeMap");
        out.println("# Generated local time: " + LocalDateTime.now());
        out.println("# Input sizes n: from " + N_MIN + " to " + N_MAX + " inclusive, step " + N_STEP);
        out.println("# Random seed (keys + shuffle orders): " + BENCHMARK_RANDOM_SEED);
        out.println("# Units: all timing columns are nanoseconds (ns)");
        out.println("#");
        out.println("# Column definitions:");
        out.println("#   size_n — number of keys inserted / searched / deleted in that row");
        out.println("#   input_pattern — RANDOM | ASCENDING | DESCENDING | PARTIALLY_SORTED");
        out.println("#   map_implementation — which map was timed");
        out.println("#   insert_batch_ns — one timer around all put() calls on an empty map");
        out.println("#   insert_single_calls_sum_ns — sum of per-put timers on a separate empty map");
        out.println("#   search_successful_ns — get() for each key in random order (all exist)");
        out.println("#   search_unsuccessful_ns — get() for keys guaranteed absent from the map");
        out.println("#   inorder_traversal_ns — iterate entrySet() once (sorted key order)");
        out.println("#   delete_all_ns — remove() each key in random order until empty");
        out.println("#");
        out.println("size_n,input_pattern,map_implementation,insert_batch_ns,insert_single_calls_sum_ns,"
                + "search_successful_ns,search_unsuccessful_ns,inorder_traversal_ns,delete_all_ns");
    }

    private static void runBenchmarkRows(PrintWriter out, Random rng) {
        for (int n = N_MIN; n <= N_MAX; n += N_STEP) {
            for (InputPattern pattern : InputPattern.values()) {
                List<Integer> keys = makeKeys(n, pattern, rng);
                List<Integer> searchOrder = shuffledCopy(keys, rng);
                List<Integer> deleteOrder = shuffledCopy(keys, rng);
                List<Integer> missing = absentKeys(n);

                treap<Integer, Integer> treapSingle = new treap<>();
                treap<Integer, Integer> treapMain = new treap<>();
                long tInsSingle = measureInsertSingleCalls(treapSingle, keys);
                long tIns = measureInsert(treapMain, keys);
                long tHit = measureSearch(treapMain, searchOrder);
                long tMiss = measureSearch(treapMain, missing);
                long tWalk = measureInorder(treapMain);
                long tDel = measureDelete(treapMain, deleteOrder);
                writeCsvRow(out, n, pattern, "treap", tIns, tInsSingle, tHit, tMiss, tWalk, tDel);

                AVLTreeMap<Integer, Integer> avlSingle = new AVLTreeMap<>();
                AVLTreeMap<Integer, Integer> avlMain = new AVLTreeMap<>();
                tInsSingle = measureInsertSingleCalls(avlSingle, keys);
                tIns = measureInsert(avlMain, keys);
                tHit = measureSearch(avlMain, searchOrder);
                tMiss = measureSearch(avlMain, missing);
                tWalk = measureInorder(avlMain);
                tDel = measureDelete(avlMain, deleteOrder);
                writeCsvRow(out, n, pattern, "AVLTreeMap", tIns, tInsSingle, tHit, tMiss, tWalk, tDel);

                TreeMap<Integer, Integer> javaSingle = new TreeMap<>();
                TreeMap<Integer, Integer> javaMain = new TreeMap<>();
                tInsSingle = measureInsertSingleCalls(javaSingle, keys);
                tIns = measureInsert(javaMain, keys);
                tHit = measureSearch(javaMain, searchOrder);
                tMiss = measureSearch(javaMain, missing);
                tWalk = measureInorder(javaMain);
                tDel = measureDelete(javaMain, deleteOrder);
                writeCsvRow(out, n, pattern, "java.util.TreeMap", tIns, tInsSingle, tHit, tMiss, tWalk, tDel);
            }
        }
    }

    private static void writeCsvRow(PrintWriter out, int n, InputPattern pattern, String mapName,
            long ins, long insSingle, long hit, long miss, long walk, long del) {
        out.printf("%d,%s,%s,%d,%d,%d,%d,%d,%d%n",
                n, pattern.name(), mapName, ins, insSingle, hit, miss, walk, del);
    }
}
