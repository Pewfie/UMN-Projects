package main.script;

import main.logfile.Event;
import main.script.operation.Operation;

import java.util.ArrayList;
import java.util.List;

public class Script {

    private List<Operation> ops = new ArrayList<Operation>();

    public List<Operation> getOps() {
        return ops;
    }

    public boolean addOp(Operation operation) {
        return ops.add(operation);
    }

    public List<Event> alert(List<Event> events) {
        List<Event> output = events;
        for (Operation op : ops) {
            if (op == null) {
                System.err.println("ERROR: currOp is null");
                return null;
            } else {
                output = op.applyOp(output);
            }
        }
        return output;
    }
}