package main.ui;
import main.controller.Controller;

import java.io.File;
import java.util.Scanner;

public class UI {

    private int uiMode; //0 = command line interface, 1 = graphic interface
    private Controller controller;

    public UI() {
        uiMode = 0;
        controller = new Controller();
    }

    private static boolean fileExists(String filePath) { //Method to determine whether a file exists, uses import java.io.File
        File file = new File(filePath);
        return file.exists();
    }

    public boolean addLogFile(String filename) {
        if(fileExists(filename)) {
            //add to controller
            return controller.addLogFile(new File(filename));
        }
        return false;
    }

    public boolean addScriptFile(String filename) {
        if(fileExists(filename)) {
            //add to controller
            return controller.addScriptFile(new File(filename));
        }
        return false;
    }

    public boolean addConfigFile(String filename) {
        if(fileExists(filename)) {
            //add to controller
            return controller.addConfigFile(new File(filename));
        }
        return false;
    }

    public boolean setUIMode(int mode) { //user not able to do this method in the shell as there is no need yet
        if (mode == 1 || mode == 0) {
            uiMode = mode;
            return true;
        }
        System.err.println("Error: UI - mode must be either 0 or 1");
        return false; //Invalid mode (must be either 0 or 1)
    }

    public boolean viewFile(String filename) {
        if(uiMode == 1) {
            //GUI not implemented yet, should return true on success
            return false;
        }
        return false;
    }

    public boolean startGUI() {
        return false; //GUI not implemented yet
    }

    public boolean endGUI() {
        return false; //GUI not implemented yet
    }
    public void runDefault() {
        String path = "D:\\School\\5801\\example files";
        System.out.println("Running auto");
        addLogFile(path+"/search_test_1.csv");
        addScriptFile(path+"/example_script_search.txt");
        addConfigFile(path+"/example_config_file.csv");
        controller.startAnalysis();
    }
    public void startShell() { // Will run until exited or switched to GUI mode
        if(uiMode != 0) {
            System.err.println("Incorrect UI mode");
            return;
        } 
        Scanner scan = new Scanner(System.in);
        boolean finished = false;
        while (!finished) {
            System.out.println("Input number for command or type 'stop':\n1) Input log file\n2) Input script file\n3) Add config file\n4) Start LOAFR");
            String input = scan.nextLine();
            if (input.equals("stop")) {
                System.out.println("Stopping...");
                scan.close();
                finished = true;
                break;
            }
            int option = -1;
            try {
                option = Integer.parseInt(input);
            } catch(Exception e) {
                System.out.println("Invalid input\n");
                continue;
            }
            switch(option) {
                case 1: System.out.println("Enter log file path: ");
                    input = scan.nextLine();
                    System.out.println((addLogFile(input)) ? "Log file added" : "Could not add log file");
                    break;
                case 2: System.out.println("Enter script file path: ");
                    input = scan.nextLine();
                    System.out.println((addScriptFile(input)) ? "Script file added" : "Could not add script file");
                    break;
                case 3: System.out.println("Enter config file path: ");
                    input = scan.nextLine();
                    System.out.println((addConfigFile(input)) ? "Config file added" : "Could not add config file");
                    break;
                case 4: System.out.println("Starting analysis...");
                    controller.startAnalysis();
                    return;
                case 9: runDefault();
                    return;
                default: System.out.println("Invalid option"); break;     
            }
            System.out.println(); //Just an empty line for readability
        }
        scan.close();
    }

}
