package tests.fileutil;

import main.fileutil.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.*;

public class LogfileReaderTests {

    private static final String TEST_FILE_PATH = "example files/new_example_logfile.csv";
    private File tempFile;
    private LogfileReader logfileReader;

    @Before
    public void setUp() {
        // Create a temporary log file with sample data
        tempFile = new File(TEST_FILE_PATH);
        logfileReader = new LogfileReader();
    }

    @Test
    public void testSetCurrentFile() {
        // Test 1: setting a valid log file
        assertTrue(logfileReader.setCurrentFile(tempFile));

        // Test 2: setting a null file
        assertFalse(logfileReader.setCurrentFile(null));

        // Test 3: setting a non-existing file
        File nonExistingFile = new File("nonExistingFile.txt");
        assertFalse(logfileReader.setCurrentFile(nonExistingFile));
    }
    
    //Test 1: correctly read file
    @Test
    public void testReadLine() {
        // Set the current file to the temporary log file
        assertTrue(logfileReader.setCurrentFile(tempFile));

        // Read line from the file
        List<String> line = logfileReader.readLine();
        

        // Verify the content of line
        assertEquals(List.of("Timestamp", "Event Source", "Event Name",
                "Data item 1","Data item 2","Data item 3","Data item 4"), line);
    }
    
    //Test 2: invalid file
    @Test
    public void testReadLineWithInvalidFile() {
        // Set the current file to an invalid file
        File invalidFile = new File("invalidFile.csv");
        assertFalse(logfileReader.setCurrentFile(invalidFile));

        // Reading from an invalid file should return null
        assertNull(logfileReader.readLine());
    }

}
