package main.logfile;

import java.util.List;

public class Event {
    // Note that every variable is a String because it makes comparisons for analysis functions significantly less
    // complicated.
    private String time, eventSource, eventName;

    private List<DataItem> dataItems;

    public Event(String time, String eventSource, String eventName, List<DataItem> dataItems) {
        this.time = time;
        this.eventSource = eventSource;
        this.eventName = eventName;
        this.dataItems = dataItems;
    }

    public String getTime() {
        return time;
    }
    public String getEventName() {
        return eventName;
    }
    public String getEventSource() { return eventSource; }
    public List<DataItem> getDataItems() {
        return dataItems;
    }
    public DataItem getDataItem(int i) {
        return dataItems.get(i);
    }

    public boolean addDataItem(DataItem d) {
        if (d == null) {
            return false;
        }
        return dataItems.add(d);
    }
}
