package main.script.operation;

import main.logfile.DataItem;
import main.logfile.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Filter extends Operation {
    public Filter(String op) {
        this.op = op;
    }
    
    public List<Event> applyOp(List<Event> events) {
        if (!op.startsWith("FILTER ")) { //assuming is in format FILTER <time or eventName> <operator> <value>
            System.err.println("Filter Usage: FILTER <key> <operator> <value>");
            return null;
        }

        if (events == null) {
            System.err.println("ERROR: events list is null, no operations will be performed");
            return null;
        }

        op = op.replaceFirst("FILTER ", "");
        String[] filterParts = op.split(" ", 3); // Split into at most 3 parts

        if (filterParts.length != 3) {
            System.err.println("Filter Usage: FILTER <key> <operator> <value>");
            return null;
        }

        String key = filterParts[0];
        String operator = filterParts[1];
        String value = filterParts[2];

        List<Event> eventResults = new ArrayList<>();
        for (Event event : events) {
            if (matchesFilterCriteria(event, key, operator, value)) {
                eventResults.add(event);
            }
        }

        return eventResults;
    }

    private boolean matchesFilterCriteria(Event event, String key, String operator, String value) {
        List<DataItem> itemSuccess = new ArrayList<>();
        List<DataItem> dataItems = event.getDataItems();
        if (dataItems != null) {
            for (DataItem item : dataItems) {
                if (item.getName().equals(key)) {
                    if (isNumeric(item.getValue()) && isNumeric(value)) {
                        double eventValue = Double.parseDouble(item.getValue());
                        double comparisonValue = Double.parseDouble(value);
                        if (compareNumericValues(eventValue, operator, comparisonValue))
                            itemSuccess.add(item);
                    } else {
                        if (compareStringValues(item.getValue(), operator, value))
                            itemSuccess.add(item);
                    }
                    return !itemSuccess.isEmpty();
                }
            }
        }

        if ("time".equals(key)){ //filter by time
            double eventTime = Double.parseDouble(event.getTime());
            double comparisonValue = Double.parseDouble(value);
            return compareNumericValues(eventTime, operator, comparisonValue);
        }

        return false;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean compareNumericValues(double eventValue, String operator, double comparisonValue) {
        switch (operator) {
            case "=": return eventValue == comparisonValue;
            case "<": return eventValue < comparisonValue;
            case ">": return eventValue > comparisonValue;
            case "<>": return eventValue != comparisonValue;
            default:
                System.err.println("Invalid operator for numerical comparison: " + operator);
                return false;
        }
    }

    private boolean compareStringValues(String eventValue, String operator, String comparisonValue) {
        switch (operator) {
            case "=": return eventValue.equals(comparisonValue);
            case "<>": return !eventValue.equals(comparisonValue);
            default:
                System.err.println("Invalid operator for string comparison: " + operator);
                return false;
        }
    }

}
