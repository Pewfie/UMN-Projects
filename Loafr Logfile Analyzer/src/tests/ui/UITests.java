package tests.ui;

import main.fileutil.LogfileReader;
import main.ui.UI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Scanner;

public class UITests {
    private UI ui;
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;
    @Before
    public void setUp() {
        ui = new UI();
        sysInBackup = System.in;
        sysOutBackup = System.out;
    }

    @After
    public void tearDown() {
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test
    public void testAddLogFile() {
        // Test 1: no file
        Assert.assertFalse(ui.addLogFile("doesNotExist"));

        // Test 2: found file
        Assert.assertTrue(ui.addLogFile("example files/example_logfile.csv"));
    }

    @Test
    public void testAddScriptFile() {
        // Test 1: found file
        Assert.assertTrue(ui.addScriptFile("example files/example_script_search.txt"));

        // Test 2: no file
        Assert.assertFalse(ui.addScriptFile("doesNotExist"));
    }

    @Test
    public void testAddConfigFile() {
        // Test 1: found file
        Assert.assertTrue(ui.addConfigFile("example files/example_config_file.csv"));

        // Test 2: no file
        Assert.assertFalse(ui.addConfigFile("doesNotExist"));
    }

    @Test
    public void testSetUIMode() {
        // Test 1: UI mode 0
        Assert.assertTrue(ui.setUIMode(0));

        // Test 2: UI mode 2
        Assert.assertFalse(ui.setUIMode(2));
    }

    @Test
    public void testStartShell() {
        // Test 1: "stop" as input
        ByteArrayInputStream in = new ByteArrayInputStream("stop\n".getBytes());
        System.setIn(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        ui.startShell();
        Assert.assertTrue(out.toString().contains("Stopping..."));

        // Test 2: "1" as input
        ByteArrayInputStream in2 = new ByteArrayInputStream("1\nexample files/example_logfile.csv\nstop\n".getBytes());
        System.setIn(in2);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out2));
        ui.startShell();
        Assert.assertTrue(out2.toString().contains("Log file added"));

        // Test 3: "2" as input
        ByteArrayInputStream in3 = new ByteArrayInputStream("2\nexample files/example_script_search.txt\nstop\n".getBytes());
        System.setIn(in3);
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out3));
        ui.startShell();
        Assert.assertTrue(out3.toString().contains("Script file added"));

        // Test 4: "3" as input
        ByteArrayInputStream in4 = new ByteArrayInputStream("3\nexample files/example_config_file.csv\nstop\n".getBytes());
        System.setIn(in4);
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out4));
        ui.startShell();
        Assert.assertTrue(out4.toString().contains("Config file added"));

        // Test 5: Full analysis ("4" as input)
        ByteArrayInputStream in5 = new ByteArrayInputStream("4\nstop\n".getBytes());
        System.setIn(in5);
        ByteArrayOutputStream out5 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out5));
        ui.startShell();
        Assert.assertTrue(out5.toString().contains("Starting analysis...") && out5.toString().contains("1 results found"));

        // Test 6: Couldn't add log file
        ByteArrayInputStream in6 = new ByteArrayInputStream("1\ndoesnotexist\nstop\n".getBytes());
        System.setIn(in6);
        ByteArrayOutputStream out6 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out6));
        ui.startShell();
        Assert.assertTrue(out6.toString().contains("Could not add log file"));

        // Test 7: Couldn't add script file
        ByteArrayInputStream in7 = new ByteArrayInputStream("2\ndoesnotexist\nstop\n".getBytes());
        System.setIn(in7);
        ByteArrayOutputStream out7 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out7));
        ui.startShell();
        Assert.assertTrue(out7.toString().contains("Could not add script file"));

        // Test 8: Couldn't add config file
        ByteArrayInputStream in8 = new ByteArrayInputStream("3\ndoesnotexist\nstop\n".getBytes());
        System.setIn(in8);
        ByteArrayOutputStream out8 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out8));
        ui.startShell();
        Assert.assertTrue(out8.toString().contains("Could not add config file"));

        // Test 9: Invalid input
        ByteArrayInputStream in9 = new ByteArrayInputStream("-1\nstop\n".getBytes());
        System.setIn(in9);
        ByteArrayOutputStream out9 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out9));
        ui.startShell();
        Assert.assertTrue(out9.toString().contains("Invalid option"));
    }
}
