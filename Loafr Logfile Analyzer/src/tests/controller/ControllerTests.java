package tests.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.lang.reflect.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import main.controller.Controller;
import main.logfile.*;

public class ControllerTests {
    Controller c;

    @Before
    public void setUp() {
        c = new Controller();
    }


    @Test
    public void testAddScriptFile() {
        File f = new File("example files/example_script_search.txt");
        Assert.assertTrue(c.addScriptFile(f));
    }

    @Test
    public void testAddLogFile() {
        File f = new File("example files/example_logfile.csv");
        System.out.println(f.exists());
        Assert.assertTrue(c.addScriptFile(f));
    }

}
