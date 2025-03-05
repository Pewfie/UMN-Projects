package tests.fileutil;

import main.logfile.Event;
import main.logfile.DataItem;
import main.logfile.Logfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

import main.fileutil.LogfileWriter;


public class LogfileWriterTests {

    private static final String TEST_FILE_PATH = "tempLogTest.csv";
    private LogfileWriter logFileWriter;
    private File temp;

    @Before
    public void setUp() throws IOException {
        // Create an instance of LogfileWriter before each test
        logFileWriter = new LogfileWriter();
        temp = new File(TEST_FILE_PATH);
        temp.createNewFile();
    }

    @After
    public void tearDown() {
        temp.delete();
    }

    @Test
    public void testSetCurrentFile() {
        // Test1: setting a valid log file
        assertTrue(logFileWriter.setCurrentFile(temp));

        // Test2: setting a null file
        assertFalse(logFileWriter.setCurrentFile(null));

        // Test3: setting a non-existing file
        File nonExistingFile = new File("nonExistingFile.txt");
        assertFalse(logFileWriter.setCurrentFile(nonExistingFile));
    }

    @Test
    public void testWriteEntry() {
        // Set the current log file to a temporary file
        assertTrue(logFileWriter.setCurrentFile(temp));

        // Write an entry to the log file
        List<DataItem> ds = new ArrayList<>();
        ds.add(new DataItem("Reason","PowerUp"));
        Event testData = new Event("1", "Test system", "StartUp",ds);
        assertTrue(logFileWriter.writeEntry(testData));

        // Test1: correctly writes the entries into logfile
        try {
            List<String> lines = Files.readAllLines(Path.of(TEST_FILE_PATH));
            assertEquals(1, lines.size());
            assertEquals("1,Test system,StartUp,Reason=PowerUp", lines.get(0));
        } catch (IOException e) {
            fail("Error reading the log file: " + e.getMessage());
        }

        // Test2: writing with null data
        assertFalse(logFileWriter.writeEntry(null));

        // Test3: writing without setting the current log file
        logFileWriter = new LogfileWriter(); // create a new instance to reset the currentFile
        assertFalse(logFileWriter.writeEntry(testData));
    }
}
