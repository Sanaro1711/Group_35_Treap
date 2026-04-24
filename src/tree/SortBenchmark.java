package tree;

import CsvGeneration.ArraySlicer;
import CsvGeneration.CsvReader;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public final class SortBenchmark {

    private static void pqSort(Integer[] arr) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(arr.length);
        for (Integer x : arr) pq.add(x);
        for (int i = 0; i < arr.length; i++) arr[i] = pq.poll();
    }

    private static long[] measure(Consumer<Integer[]> sort, Integer[] data) {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long memBefore = rt.totalMemory() - rt.freeMemory();
        long t0 = System.nanoTime();
        sort.accept(data);
        long time = System.nanoTime() - t0;
        rt.gc();
        long memAfter = rt.totalMemory() - rt.freeMemory();
        return new long[]{ time, memAfter - memBefore };
    }

    public static void main(String[] args) throws Exception {
        CsvReader reader = new CsvReader();
        int[] random       = reader.readCsv("src/CsvGeneration/random.csv");
        int[] sorted       = reader.readCsv("src/CsvGeneration/sorted.csv");
        int[] reverse      = reader.readCsv("src/CsvGeneration/reverse_sorted.csv");
        int[] nearlySorted = reader.readCsv("src/CsvGeneration/nearly_sorted.csv");

        String[] algos = {"TreapSort", "PQSort", "Java_TimSort", "QuickSort", "MergeSort"};

        Consumer<Integer[]>[] sorters = new Consumer[]{
                (Consumer<Integer[]>) treap::treapSort,
                (Consumer<Integer[]>) SortBenchmark::pqSort,
                (Consumer<Integer[]>) Arrays::sort,
                (Consumer<Integer[]>) a -> new SortingAlgorithms().quick_sort((Integer[]) a, 0, ((Integer[]) a).length - 1),
                (Consumer<Integer[]>) a -> new SortingAlgorithms().merge_sort((Integer[]) a, 0, ((Integer[]) a).length - 1)
        };

        String header = "input_size," +
                "random_time,sorted_time,nearly_sorted_time,reverse_sorted_time," +
                "random_memory_bytes,sorted_memory_bytes,nearly_sorted_memory_bytes,reverse_sorted_memory_bytes\n";

        for (int i = 0; i < algos.length; i++) {
            new File("src/benchmark_output/sorting").mkdirs();
            FileWriter writer = new FileWriter(new File("src/benchmark_output/sorting/" + algos[i] + ".csv"));
            writer.write(header);

            for (int n = 100; n <= 10000; n += 100) {
                long[] rand  = measure(sorters[i], ArraySlicer.slice(random,       n));
                long[] sortd  = measure(sorters[i], ArraySlicer.slice(sorted,       n));
                long[] near = measure(sorters[i], ArraySlicer.slice(nearlySorted, n));
                long[] rev  = measure(sorters[i], ArraySlicer.slice(reverse,      n));

                writer.write(n + "," +
                        rand[0]  + "," + sortd[0]  + "," + near[0]  + "," + rev[0]  + "," +
                        rand[1]  + "," + sortd[1]  + "," + near[1]  + "," + rev[1]  + "\n");
            }

            writer.close();
        }
    }
}