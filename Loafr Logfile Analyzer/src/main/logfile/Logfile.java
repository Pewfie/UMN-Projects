package main.logfile;

import java.util.List;
import java.util.Map;

public class Logfile {
    // Key is event name, value is event data type
    private Map<String, String> headers;
    private List<Event> events;

    public Logfile(Map<String, String> headers, List<Event> events) {
        this.headers = headers;
        this.events = events;
    }

    public boolean addEvent(Event event) {
        if (event == null) {
            return false;
        }

        return events.add(event);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
