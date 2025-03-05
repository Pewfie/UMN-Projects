package main.controller;
import main.fileutil.ConfigFileReader;
import main.fileutil.LogfileReader;
import main.fileutil.LogfileWriter;
import main.fileutil.ScriptParser;
import main.logfile.Event;
import main.logfile.Logfile;
import main.script.Script;
import java.io.File;
import java.util.Map;
import java.util.List;
//The design document was unclear on the parameters for writeLogEntry() and readLogFileEvent()
// readLogFileEvent said to return a list or a boolean, i went with List<String> and writeLogEntry now takes an Event as a parameter
public class ControllerPipe {

    private LogfileReader logfileReader;
    private ScriptParser scriptParser;
    private ConfigFileReader configFileReader;
    private LogfileWriter logfileWriter;

    public ControllerPipe() {
        logfileReader = new LogfileReader();
        scriptParser = new ScriptParser();
        configFileReader = new ConfigFileReader();
        logfileWriter = new LogfileWriter();
    }

    public Script parseScript(File script_file) {
        return scriptParser.parseScript(script_file);
    }

    public boolean setCurrentLogFileR(File logfile) {
        return logfileReader.setCurrentFile((logfile));
    }

    public boolean setCurrentLogFileW(File logfile) {
        return logfileWriter.setCurrentFile(logfile);
    }

    public List<String> readLogFileEvent() {
        return logfileReader.readLine();
    }

    public Logfile readLogFile(Logfile lf) { return logfileReader.readAll(lf); }

    public Map<String,String> readConfigFile(File configfile) {
        return configFileReader.readConfigFile(configfile);
    }

    public boolean writeLogEntry(Event event) {
        return logfileWriter.writeEntry(event);
    }
}
