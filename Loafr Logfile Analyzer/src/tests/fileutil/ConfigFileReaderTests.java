package tests.fileutil;

import main.fileutil.*;
import org.junit.Test;

import java.io.*;
import java.util.Map;
import static org.junit.Assert.*;

public class ConfigFileReaderTests {

    @Test
    public void testReadConfigFile() {
        // Create a temporary config file with sample data
        File tempFile = createTempConfigFile("data name,data type\nthermometer,decimal\nswitch,boolean\nvoltage,decimal\ncount,integer");

        ConfigFileReader configFileReader = new ConfigFileReader();

        // Read the config file
        Map<String, String> configMap = configFileReader.readConfigFile(tempFile);

        // Test 1: correct convert config file into dictionary
        assertNotNull(configMap);
        assertEquals(5, configMap.size());
        assertEquals("decimal", configMap.get("thermometer"));
        assertEquals("boolean", configMap.get("switch"));
        assertEquals("decimal", configMap.get("voltage"));
        assertEquals("integer", configMap.get("count"));

        // Test 2: invalid format in the config file
        File invalidTempFile = createTempConfigFile("thermometer, ");
        Map<String, String> invalidConfigMap = configFileReader.readConfigFile(invalidTempFile);
        assertNull(invalidConfigMap);

        // Test 3: non-existing file
        File nonExistingFile = new File("nonExistingFile.csv");
        Map<String, String> nonExistingConfigMap = configFileReader.readConfigFile(nonExistingFile);
        assertNull(nonExistingConfigMap);
    }

    private File createTempConfigFile(String content) {
        try {
            File tempFile = File.createTempFile("tempConfig", ".csv");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(content);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp config file", e);
        }
    }
}

