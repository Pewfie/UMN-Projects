package tests.script;

import main.logfile.DataItem;
import main.logfile.Event;
import main.script.Script;
import main.script.operation.Operation;
import main.script.operation.Search;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ScriptTests {
    Script script = new Script();


    @Test
    public void testAlert() {
        List<Event> events = new ArrayList<>();
        List<DataItem> dataItems = new ArrayList<>();
        dataItems.add(new DataItem("boolean","true"));

        events.add(new Event("0","start", "start", dataItems));
        events.add(new Event("1", "thermometer", "thermometer", dataItems));
        events.add(new Event("4", "switch", "switch", dataItems));
        events.add(new Event("4", "voltage", "voltage",dataItems));
        events.add(new Event("5", "count", "count",dataItems));
        events.add(new Event("10", "count", "count",dataItems));
        events.add(new Event("15", "count", "count",dataItems));

        // Test 1: Null currOp
        Assert.assertNull(script.alert(events));

        // Test 2: Null events
        Assert.assertNull(script.alert(null));

        // Test 3: applyOp failure
        Operation operation = new Search("");
        script.addOp(operation);
        Assert.assertNull(script.alert(events));

        // Test 4: successful applyOp
        Operation operation1 = new Search("SEARCH count=2");
        script.addOp(operation1);
        List<Event> newEvents = script.alert(events);
        List<Event> correctEvents = new ArrayList<>();
        correctEvents.add(events.get(5));
        Assert.assertEquals(correctEvents.get(0), newEvents.get(0));
    }
}
