package tree;

import CsvGeneration.ArraySlicer;
import CsvGeneration.CsvReader;
import interfaces.Entry;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public final class MeasurePerformance {
    private static final String[] MAPS = { "Treap", "AVLTreeMap", "JavaTreeMap" };

    // absent keys are guaranteed not to be in any of the CSVs since values are 10000 or less
    private static Integer[] absentKeys(int n) {
        Integer[] keys = new Integer[n];
        for (int i = 0; i < n; i++) keys[i] = 100000 + i;
        return keys;
    }

    // ----- measure helpers -----

    private static long insertBatch(interfaces.Map<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.put(k, k);
        }
        return System.nanoTime() - time;
    }

    private static long insertSingle(interfaces.Map<Integer, Integer> map, Integer[] keys) {
        long total = 0;
        for (Integer k : keys) {
            long time = System.nanoTime();
            map.put(k, k);
            total += System.nanoTime() - time;
        }
        return total;
    }

    private static long searchHit(interfaces.Map<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.get(k);
        }
        return System.nanoTime() - time;
    }

    private static long searchMiss(interfaces.Map<Integer, Integer> map, int n) {
        Integer[] absent = absentKeys(n);
        long time = System.nanoTime();
        for (Integer k : absent) {
            map.get(k);
        }
        return System.nanoTime() - time;
    }

    private static long delete(interfaces.Map<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.remove(k);
        }
        return System.nanoTime() - time;
    }

    private static long inorder(interfaces.Map<Integer, Integer> map) {
        long time = System.nanoTime();
        for (Entry<Integer, Integer> e : map.entrySet()) {
            int ignored = e.getKey();
        }
        return System.nanoTime() - time;
    }

    // java.util.TreeMap overloads

    private static long insertBatch(java.util.TreeMap<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.put(k, k);
        }
        return System.nanoTime() - time;
    }

    private static long insertSingle(java.util.TreeMap<Integer, Integer> map, Integer[] keys) {
        long total = 0;
        for (Integer k : keys) {
            long time = System.nanoTime();
            map.put(k, k);
            total += System.nanoTime() - time;
        }
        return total;
    }

    private static long searchHit(java.util.TreeMap<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.get(k);
        }
        return System.nanoTime() - time;
    }

    private static long searchMiss(java.util.TreeMap<Integer, Integer> map, int n) {
        Integer[] absent = absentKeys(n);
        long time = System.nanoTime();
        for (Integer k : absent) {
            map.get(k);
        }
        return System.nanoTime() - time;
    }

    private static long delete(java.util.TreeMap<Integer, Integer> map, Integer[] keys) {
        long time = System.nanoTime();
        for (Integer k : keys) {
            map.remove(k);
        }
        return System.nanoTime() - time;
    }

    private static long inorder(java.util.TreeMap<Integer, Integer> map) {
        long time = System.nanoTime();
        for (var e : map.entrySet()) {
            int ignored = e.getKey();
        }
        return System.nanoTime() - time;
    }

    // one map run for each size of n
    private static void runProjectMap(FileWriter writer, int[] random, int[] sorted,
                                      int[] reverse, int[] nearly, String mapName) throws Exception {

        for (int n = 100; n <= 10000; n += 100) {
            Integer[] rand  = ArraySlicer.slice(random,  n);
            Integer[] sortd  = ArraySlicer.slice(sorted,  n);
            Integer[] rev  = ArraySlicer.slice(reverse, n);
            Integer[] near = ArraySlicer.slice(nearly,  n);

            interfaces.Map<Integer,Integer> m1 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> m2 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> m3 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> m4 = newProjectMap(mapName);

            long randIns  = insertBatch(m1, rand);
            long sortdIns  = insertBatch(m2, sortd);
            long revIns  = insertBatch(m3, rev);
            long nearIns = insertBatch(m4, near);

            interfaces.Map<Integer,Integer> s1 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> s2 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> s3 = newProjectMap(mapName);
            interfaces.Map<Integer,Integer> s4 = newProjectMap(mapName);
            long randInsSingle  = insertSingle(s1, rand);
            long sortdInsSingle  = insertSingle(s2, sortd);
            long revInsSingle  = insertSingle(s3, rev);
            long nearInsSingle = insertSingle(s4, near);

            long randHit  = searchHit(m1, rand);
            long sortdHit  = searchHit(m2, sortd);
            long revHit  = searchHit(m3, rev);
            long nearHit = searchHit(m4, near);

            long randMiss  = searchMiss(m1, n);
            long sortdMiss  = searchMiss(m2, n);
            long revMiss  = searchMiss(m3, n);
            long nearMiss = searchMiss(m4, n);

            long randInord  = inorder(m1);
            long sortdInord  = inorder(m2);
            long revInord  = inorder(m3);
            long nearInord = inorder(m4);

            long randDel  = delete(m1, rand);
            long sortdDel  = delete(m2, sortd);
            long revDel  = delete(m3, rev);
            long nearDel = delete(m4, near);

            writer.write(n + ","
                    + randIns  + "," + sortdIns  + "," + nearIns  + "," + revIns  + ","
                    + randInsSingle + "," + sortdInsSingle + "," + nearInsSingle + "," + revInsSingle + ","
                    + randHit  + "," + sortdHit  + "," + nearHit  + "," + revHit  + ","
                    + randMiss + "," + sortdMiss + "," + nearMiss + "," + revMiss + ","
                    + randInord + "," + sortdInord + "," + nearInord + "," + revInord + ","
                    + randDel  + "," + sortdDel  + "," + nearDel  + "," + revDel
                    + "\n");
        }
    }

    private static void runJavaTreeMap(FileWriter writer, int[] random, int[] sorted,
                                       int[] reverse, int[] nearly) throws Exception {

        for (int n = 100; n <= 10000; n += 100) {
            Integer[] rand  = ArraySlicer.slice(random,  n);
            Integer[] sortd  = ArraySlicer.slice(sorted,  n);
            Integer[] rev  = ArraySlicer.slice(reverse, n);
            Integer[] near = ArraySlicer.slice(nearly,  n);

            java.util.TreeMap<Integer,Integer> m1 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> m2 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> m3 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> m4 = new java.util.TreeMap<>();

            long randIns  = insertBatch(m1, rand);
            long sortdIns  = insertBatch(m2, sortd);
            long revIns  = insertBatch(m3, rev);
            long nearIns = insertBatch(m4, near);

            java.util.TreeMap<Integer,Integer> s1 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> s2 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> s3 = new java.util.TreeMap<>();
            java.util.TreeMap<Integer,Integer> s4 = new java.util.TreeMap<>();

            long randInsSingle  = insertSingle(s1, rand);
            long sortdInsSingle  = insertSingle(s2, sortd);
            long revInsSingle  = insertSingle(s3, rev);
            long nearInsSingle = insertSingle(s4, near);

            long randHit  = searchHit(m1, rand);
            long sortdHit  = searchHit(m2, sortd);
            long revHit  = searchHit(m3, rev);
            long nearHit = searchHit(m4, near);

            long randMiss  = searchMiss(m1, n);
            long sortdMiss  = searchMiss(m2, n);
            long revMiss  = searchMiss(m3, n);
            long nearMiss = searchMiss(m4, n);

            long randInord  = inorder(m1);
            long sortdInord  = inorder(m2);
            long revInord  = inorder(m3);
            long nearInord = inorder(m4);

            long randDel  = delete(m1, rand);
            long sortdDel  = delete(m2, sortd);
            long revDel  = delete(m3, rev);
            long nearDel = delete(m4, near);

            writer.write(n + ","
                    + randIns  + "," + sortdIns  + "," + nearIns  + "," + revIns  + ","
                    + randInsSingle + "," + sortdInsSingle + "," + nearInsSingle + "," + revInsSingle + ","
                    + randHit  + "," + sortdHit  + "," + nearHit  + "," + revHit  + ","
                    + randMiss + "," + sortdMiss + "," + nearMiss + "," + revMiss + ","
                    + randInord + "," + sortdInord + "," + nearInord + "," + revInord + ","
                    + randDel  + "," + sortdDel  + "," + nearDel  + "," + revDel
                    + "\n");
        }
    }

    private static interfaces.Map<Integer, Integer> newProjectMap(String name) {
        switch (name) {
            case "Treap":      return new treap<>();
            case "AVLTreeMap": return new AVLTreeMap<>();
            default:           throw new IllegalArgumentException(name);
        }
    }

    private static final String HEADER =
            "input_size," +
                    "random_insert_batch,sorted_insert_batch,nearly_sorted_insert_batch,reverse_insert_batch," +
                    "random_insert_single,sorted_insert_single,nearly_sorted_insert_single,reverse_insert_single," +
                    "random_search_hit,sorted_search_hit,nearly_sorted_search_hit,reverse_search_hit," +
                    "random_search_miss,sorted_search_miss,nearly_sorted_search_miss,reverse_search_miss," +
                    "random_inorder,sorted_inorder,nearly_sorted_inorder,reverse_inorder," +
                    "random_delete,sorted_delete,nearly_sorted_delete,reverse_delete\n";

    public static void main(String[] args) throws Exception {
        CsvReader reader = new CsvReader();
        int[] random  = reader.readCsv("src/CsvGeneration/random.csv");
        int[] sorted  = reader.readCsv("src/CsvGeneration/sorted.csv");
        int[] reverse = reader.readCsv("src/CsvGeneration/reverse_sorted.csv");
        int[] nearly  = reader.readCsv("src/CsvGeneration/nearly_sorted.csv");

        for (String map : MAPS) {
            new File("src/benchmark_output").mkdirs();
            FileWriter writer = new FileWriter(new File("src/benchmark_output/" + map + ".csv"));
            writer.write(HEADER);
            if (map.equals("JavaTreeMap")) {
                runJavaTreeMap(writer, random, sorted, reverse, nearly);
            } else {
                runProjectMap(writer, random, sorted, reverse, nearly, map);
            }
            writer.close();
        }
    }
}