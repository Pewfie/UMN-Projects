package tests.controller;

import main.controller.ControllerPipe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ControllerPipeTests {
    private ControllerPipe pipe;
    private File script, config, logfile, invalid;
    @Before
    public void setUp() {
        pipe = new ControllerPipe();
        script = new File("example files/example_script_search.txt");
        config = new File("example files/example_config_file.csv");
        logfile = new File("example files/example_logfile.csv");
        invalid = new File("invalidPath");
    }

    @Test
    public void testParseScript() {
        // Test 1: invalid path
        Assert.assertNull(pipe.parseScript(invalid));

        // Test 2: valid path
        Assert.assertNotNull(pipe.parseScript(script));
    }

    @Test
    public void testSetCurrentLogFileR() {
        // Test 1: invalid path
        Assert.assertFalse(pipe.setCurrentLogFileR(invalid));

        // Test 2: valid path
        Assert.assertTrue(pipe.setCurrentLogFileR(logfile));
    }

    @Test
    public void testSetCurrentLogFileW() {
        // Test 1: invalid path
        Assert.assertFalse(pipe.setCurrentLogFileW(invalid));

        // Test 2: valid path
        Assert.assertTrue(pipe.setCurrentLogFileW(logfile));
    }

    @Test
    public void testReadLogFileEvent() {
        // Test 1: invalid path
        Assert.assertNull(pipe.readLogFileEvent());
    }

    @Test
    public void testReadConfigFile() {
        // Test 1: invalid path
        Assert.assertNull(pipe.readConfigFile(invalid));

        // Test 2: valid path
        Assert.assertNotNull(pipe.readConfigFile(config));
    }

    @Test
    public void testWriteLogfileEntry() {
        // Test 1: invalid path
        Assert.assertFalse(pipe.writeLogEntry(null));
    }
}
