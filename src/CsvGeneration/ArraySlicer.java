package CsvGeneration;

public class ArraySlicer {

    public static Integer[] slice(int[] arr, int n) {
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++) {
            result[i] = arr[i];
        }
        return result;
    }
}