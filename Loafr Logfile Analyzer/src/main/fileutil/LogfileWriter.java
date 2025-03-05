package main.fileutil;

import main.logfile.Event;
import main.logfile.Logfile;
import main.logfile.DataItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LogfileWriter {
	private File currentFile;
	
	public boolean setCurrentFile(File logFile) {
        if (logFile != null && logFile.exists() && logFile.isFile()) {
            currentFile = logFile;
            return true;
        } else {
            System.err.println("Invalid log file provided.");
            return false;
        }
    }
	
	public boolean writeEntry(Event event) {
        if (currentFile == null) {
            System.err.println("Log file not set. Call setCurrentFile() first.");
            return false;
        }

        if (event == null) {
            System.err.println("ERROR: null event");
            return false;
        }

        try (FileWriter writer = new FileWriter(currentFile, true)) {
        	//Join the elements of event with commas and write as a single row to the log file
            String row = String.format("%s,%s,%s", event.getTime(), event.getEventSource(), event.getEventName());
            List<DataItem> dataList = event.getDataItems();
            for (DataItem dataItem : dataList ) {
                row = String.format("%s,%s=%s", row, dataItem.getName(),dataItem.getValue());
            }
            row = String.format("%s\n", row);
            writer.write(row);

            return true;
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
            return false;
        }

	}

    public boolean writeAll(Logfile l) {
        if (currentFile == null) {
            System.err.println("Log file not set. Call setCurrentFile() first.");
            return false;
        }

        if (l == null) {
            System.err.println("ERROR: null logfile");
            return false;
        }

        try (FileWriter writer = new FileWriter(currentFile, true)) {
            // write header first
            String header = "Timestamp,Event Source,Event Name,Data item 1,Data item 2,Data item 3, Data item 4";
            writer.write(header);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
            return false;
        }

        for (Event e : l.getEvents()) {
            if (!writeEntry(e)) { return false; }
        }

        return true;

    }

}
