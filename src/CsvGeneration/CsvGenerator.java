package CsvGeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class CsvGenerator {

    public static void main(String[] args) throws IOException {

        File output = new File("C:\\Users\\boist\\IdeaProjects\\Group_35_Treap\\src\\CsvGeneration\\random.csv");
        File sorted = new File("C:\\Users\\boist\\IdeaProjects\\Group_35_Treap\\src\\CsvGeneration\\sorted.csv");
        File reverse_sorted = new File("C:\\Users\\boist\\IdeaProjects\\Group_35_Treap\\src\\CsvGeneration\\reverse_sorted.csv");
        File nearly_sorted = new File("C:\\Users\\boist\\IdeaProjects\\Group_35_Treap\\src\\CsvGeneration\\nearly_sorted.csv");
        FileWriter writer = new FileWriter(output);
        FileWriter writer1 = new FileWriter(sorted);
        FileWriter writer2 = new FileWriter(reverse_sorted);
        FileWriter writer3 = new FileWriter(nearly_sorted);

        Random r = new Random();
        int size = 10000;
        int[] arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = i;
        }
        for (int i = 0; i < size - 1; i++) {
            if (r.nextInt(50) == 0) {
                int temp = arr[i];
                arr[i] = arr[i + 1];
                arr[i + 1] = temp;
            }
        }
        for(int i = 0; i < size; i++) {
            writer1.write(String.valueOf(i+1) +"\n" );
            writer2.write(String.valueOf(size - i) +"\n");
            writer.write(String.valueOf(r.nextInt()));
            writer3.write(String.valueOf(arr[i]+1) +"\n");
            if (i < size - 1) {
                writer.write("\n");
            }
        }

        writer.close();
        writer1.close();
        writer2.close();
        writer3.close();
    }
}