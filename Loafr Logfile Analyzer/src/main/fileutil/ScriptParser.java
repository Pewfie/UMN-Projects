package main.fileutil;

import main.script.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import main.script.Script;
import main.script.operation.*;

public class ScriptParser {
	
	public Script parseScript(File scriptFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            Script s = new Script();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SEARCH")) {
                    s.addOp(new Search(line));
                }
                else if (line.contains("FILTER")) {
                    s.addOp(new Filter(line));
                }
                else {
                    System.err.println("invalid script command; valid commands: SEARCH, FILTER");
                    return null;
                }
            }

            return s;
        } catch (IOException e) {
            System.err.println("Error reading script file: " + e.getMessage());
            return null;
        }
    }

}
