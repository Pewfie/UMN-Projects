package tests;

import main.Loafr;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;


public class LoafrTests {
    @Test
    public void testMain() {
        ByteArrayInputStream in = new ByteArrayInputStream(("1\n" +
                                                            "example files/example_logfile.csv\n" +
                                                            "2\n" +
                                                            "example files/example_script_search.txt\n" +
                                                            "3\n" +
                                                            "example files/example_config_file.csv\n" +
                                                            "4\n" +
                                                            "stop\n").getBytes());
        System.setIn(in);
        InputStream sysInBackup = System.in;
        PrintStream sysOutBackup = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Loafr.main(new String[]{});
        String output = out.toString();
        Assert.assertTrue(output.contains("Starting analysis...") && output.contains("1 results found"));
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }
}
