package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DataReader {

    // Reads JSON test data. Each JSON object becomes one TestNG test iteration.
    public static Object[][] read(String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> testDataList = mapper.readValue(
                    new File(filePath), new TypeReference<List<Map<String, Object>>>() {});

            Object[][] data = new Object[testDataList.size()][1];
            for (int i = 0; i < testDataList.size(); i++) {
                data[i][0] = testDataList.get(i);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read test data file: " + filePath, e);
        }
    }
}
