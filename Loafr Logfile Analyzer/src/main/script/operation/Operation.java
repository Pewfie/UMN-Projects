package main.script.operation;

import main.logfile.Event;

import java.util.List;

public abstract class Operation {
    // The actual operation itself. For example, a search operation would be "SEARCH temperature=4".
    // Set in ScriptParser
    protected String op;
    public String getOp() {
        return op;
    }

    public abstract List<Event> applyOp(List<Event> events);
}
