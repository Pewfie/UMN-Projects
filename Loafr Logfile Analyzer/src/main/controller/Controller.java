package main.controller;

import java.util.*;
import java.io.File;
import java.io.IOException;

import main.script.Script;

// Should only need to import Logfile, if Event parsing is done in LogfileReader
import main.logfile.*;

public class Controller {
    private List<File> logfilefiles;
    private List<File> scriptfiles;
    private LinkedList<Script> scripts;
    private LinkedList<Logfile> logfiles;
    private File configfile;
    private ControllerPipe pipe;

    private String outputPath;

    public Controller() {
        logfilefiles = new ArrayList<>();
        scriptfiles = new ArrayList<>();
        pipe = new ControllerPipe();
        outputPath = "output/";
    }
    public boolean addScriptFile(File script_file) {
        scriptfiles.add(script_file);
        return true;
    }
    public boolean addConfigFile(File config_file) {
        configfile = config_file;
        return true;
    }
    public boolean addLogFile(File log_file) {
        logfilefiles.add(log_file);
        return true;
    }
    public boolean startAnalysis() {
        scripts = new LinkedList<>();
        for(File f : scriptfiles) {
            Script currScript = pipe.parseScript(f);
            if (currScript == null) {
                return false;
            }
            scripts.add(currScript);
        }
        logfiles = new LinkedList<>();
        for(File f : logfilefiles) {
            if (!runFile(f)) {
                return false;
            }
        }
        return true;
    }

    private boolean runFile(File logfile) {
        if (!pipe.setCurrentLogFileR(logfile)) {
            return false;
        }
        Logfile l = getLogfile();
        if (l == null) { return false; }
        File scriptfile = scriptfiles.get(0);
        File directory = new File(outputPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) { return false; }
        }
        for (Script s : scripts) {
            File outputFile;
            String[] logfilePath = logfile.getName().split("/");
            String logfileName = logfilePath[logfilePath.length - 1];
            String[] scriptPath = scriptfile.getName().split("/");
            String scriptName = scriptPath[scriptPath.length - 1];

            outputFile = new File(outputPath + logfileName + "_" + scriptName);
            try {
                int i = 1;
                while (outputFile.exists()) {
                    outputFile = new File(outputPath + logfileName + "_" + scriptName + "(" + i + ")");
                    i++;
                }
                if (!outputFile.createNewFile()) { return false; }
            }
            catch(IOException e) {
                System.err.println("ERROR: cannot create output file");
                e.printStackTrace();
            }
            if(!pipe.setCurrentLogFileW(outputFile)) {
                return false;
            }
            // alert should definitely be returning a logfile instead
            List<Event> output = s.alert(l.getEvents());
            // and LogfileWriter should be writing an entire logfile
            for (Event e : output) {
                if (!pipe.writeLogEntry(e)) {
                    return false;
                }
            }

            System.out.println(output.size() + " Results Found");

        }
        return true;
    }

    private Logfile getLogfile() {
        Map<String, String> typeConfig = pipe.readConfigFile(configfile);
        if (typeConfig == null) { return null; }
        Logfile l = new Logfile(typeConfig, new ArrayList<Event>());
        logfiles.add(l);
        l = pipe.readLogFile(l);
        return l;
    }
}
