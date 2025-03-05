package main.fileutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileReader {
	
	public Map<String, String> readConfigFile(File configFile) {
        Map<String, String> configMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            //skip header in config file (just says "Data item,Data type")
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                //config file has entries in the format: DataItem, DataType
                String[] parts = line.split(",");
                if (parts.length == 2 && !(parts[0].trim().isEmpty() || parts[1].trim().isEmpty())) {
                    // Add entry to the map
                    configMap.put(parts[0].trim(), parts[1].trim());
                } else {
                    System.err.println("Invalid format in config file: " + line);
                    return null;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
            return null;
        }

        return configMap;
    }


}
