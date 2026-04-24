package CsvGeneration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public int[] readCsv(String csv){
        List<Integer> column = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(csv))){
            String row;
            while ((row = br.readLine()) != null){
                String[] values = row.split(",");
                if (values.length > 0){
                    column.add(Integer.parseInt(values[0]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int[] result = new int[column.size()];
        for (int i = 0; i < column.size(); i++){
            result[i] = column.get(i);
        }
        return result;
    }
}
