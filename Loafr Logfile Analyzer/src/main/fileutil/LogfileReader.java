package main.fileutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.logfile.*;

public class LogfileReader {
	private File currentFile;
    private BufferedReader reader;
    
    public boolean setCurrentFile(File logFile) {
        if (logFile != null && logFile.exists() && logFile.isFile()) {
            currentFile = logFile;
            return true;
        } else {
            return false;
        }
    }
    
    //Initialize the BufferedReader to read from the current log file.
    private void initializeReader() throws IOException {
        if (currentFile != null) {
            reader = new BufferedReader(new FileReader(currentFile));
        }
    }
    
    //Reads one line (until a newline character is seen) from the current log file
    public List<String> readLine() {
        if (currentFile == null) {
            // If currentFile is not set, return null
            return null;
        }

        try {
            if (reader == null) {
                initializeReader();
            }

            String line = reader.readLine();
            if (line != null) {
                // Split the raw text data into a list based on comma
                return new ArrayList<>(List.of(line.split(",")));
            } else {
                // Close the reader when all lines have been read
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        return null;
    }

    private Event getEvent() {
        List<String> data_list = readLine();
        Event e;
        if (data_list == null) { return null; }
        if (data_list.size() == 1) {
            e = new Event(data_list.get(0), "","", null);
        }
        else if (data_list.size() < 3) {
            System.err.println("ERROR: event missing full definition");
            return null;
        }
        else {
            e = new Event(data_list.get(0), data_list.get(1), data_list.get(2), new ArrayList<>());
            for (int i = 3; i < data_list.size(); i++) {
                String[] dataItem = data_list.get(i).split("=");
                if (dataItem.length < 2) {
                    System.err.println("ERROR: data item missing =");
                    return null;
                }
                if (!e.addDataItem(new DataItem(dataItem[0], dataItem[1]))) {
                    return null;
                }
            }
        }
        return e;
    }

    //Reads lines from current logfile until all data is read in
    public Logfile readAll(Logfile lf) {
        if (currentFile == null) {
            return null;
        }
        // read and discard the header
        if (readLine() == null) { return null; }

        Event e = getEvent();
        while (e != null) {
            lf.addEvent(e);
            e = getEvent();
        }
        return lf;
    }
    
}
