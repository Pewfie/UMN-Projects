package main.script.operation;

import main.logfile.Event;

import java.util.ArrayList;
import java.util.List;

public class Search extends Operation {
    public Search(String op) {
        this.op = op;
    }

    private void printUsageError() {
        System.err.println("Search Usage: SEARCH <event name>\n              SEARCH <><event name>");
        System.err.println("Context Usage: SEARCH <event name> BEFORE/AFTER <event name>\n               SEARCH <event name> BETWEEN <event name 1> AND <event name 2>");
    }
    public List<Event> applyOp(List<Event> events) {
        if (!op.contains("SEARCH ")) {
            printUsageError();
            return null;
        }
        if (op.length() < 8) {  // "SEARCH " is length 7, at least length 1 for event name
            printUsageError();
            return null;
        }
        if (events == null) {
            System.err.println("ERROR: events list is null, no operations will be performed");
            return null;
        }

        op = op.replaceFirst("SEARCH ", "");
        if (op.contains(" BEFORE ") || op.contains(" AFTER ") || op.contains(" BETWEEN ") || op.contains(" AND ")) {
            return doContext(events);
        }

        return doSearch(events);
    }

    // Searches normally (SEARCH <event name>)
    private List<Event> doSearch(List<Event> events) {
        boolean not = false;
        if (op.contains("<>")) {
            not = true;
            op = op.replaceFirst("<>", "");
        }
        List<Event> eventResults = new ArrayList<>();
        for (Event event : events) {
           boolean in = op.equals(event.getEventName());
           if ((not && !in) || (!not && in)) {
               eventResults.add(event);
           }
        }

        return eventResults;
    }

    // Searches by context (BEFORE/AFTER/BETWEEN)
    private List<Event> doContext(List<Event> events) {
        if (op.contains("BEFORE")) {
            String[] keyLimit = op.split(" BEFORE ");
            if (keyLimit.length != 2) {
                System.err.println("Context \"BEFORE\" Usage: SEARCH <event name> BEFORE <event name>");
                return null;
            }

            String key = keyLimit[0];
            String limit = keyLimit[1];
            List<Event> eventResults = new ArrayList<>();
            for (Event event : events) {
                if (limit.equals(event.getEventName())) {
                    break;
                }
                if (key.equals(event.getEventName())) {
                    eventResults.add(event);
                }
            }

            return eventResults;
        }

        if (op.contains("AFTER")) {
            String[] keyLimit = op.split(" AFTER ");
            if (keyLimit.length != 2) {
                System.err.println("Context \"AFTER\" Usage: SEARCH <event name> AFTER <event name>");
                return null;
            }

            String key = keyLimit[0];
            String limit = keyLimit[1];
            List<Event> eventResults = new ArrayList<>();
            boolean reached = false;
            for (Event event : events) {
                if (limit.equals(event.getEventName())) {
                    reached = true;
                }
                if (reached && key.equals(event.getEventName())) {
                    eventResults.add(event);
                }
            }

            return eventResults;
        }

        if (op.contains("BETWEEN") && op.contains("AND")) {
            String[] keyLimit = op.split(" BETWEEN ");
            if (keyLimit.length != 2) {
                System.err.println("Context \"BETWEEN\" Usage: SEARCH <event name> BETWEEN <event name 1> AND <event name 2>");
                return null;
            }

            String[] limits = keyLimit[1].split(" AND ");
            if (limits.length != 2) {
                System.err.println("Context \"BETWEEN\" Usage: SEARCH <event name> BETWEEN <event name 1> AND <event name 2>");
                return null;
            }

            String key = keyLimit[0];
            String limit1 = limits[0];
            String limit2 = limits[1];
            List<Event> eventResults = new ArrayList<>();
            boolean reached = false;
            for (Event event : events) {
                if (limit1.equals(event.getEventName())) {
                    reached = true;
                }
                if (limit2.equals(event.getEventName())) {
                    break;
                }
                if (reached && key.equals(event.getEventName())) {
                    eventResults.add(event);
                }
            }
            return eventResults;
        }

        printUsageError();
        return null;
    }
}
