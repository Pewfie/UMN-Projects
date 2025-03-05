package tests.script.operation;

import main.logfile.DataItem;
import main.logfile.Event;
import main.script.operation.Filter;
import main.script.operation.Operation;
import main.script.operation.Search;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class FilterTests {
    @Test
    public void testApplyOp() {
        List<Event> events = new ArrayList<>();
        List<DataItem> dataItems = new ArrayList<>();
        dataItems.add(new DataItem("boolean","true"));
        dataItems.add(new DataItem("decimal","45"));
        dataItems.add(new DataItem("boolean","true"));
        dataItems.add(new DataItem("decimal","90.355"));
        dataItems.add(new DataItem("integer","1"));
        dataItems.add(new DataItem("integer","2"));
        dataItems.add(new DataItem("integer","1"));

        events.add(new Event("0","start", "start", dataItems));
        events.add(new Event("1", "thermometer", "thermometer", dataItems));
        events.add(new Event("4", "switch", "switch", dataItems));
        events.add(new Event("4", "voltage", "voltage", dataItems));
        events.add(new Event("5", "count", "count", dataItems));
        events.add(new Event("10", "count", "count", dataItems));
        events.add(new Event("15", "count", "count", dataItems));

        // Test 1: empty filter 
        Operation test1 = new Filter("");
        Assert.assertNull(test1.applyOp(events));

        // Test 2: null events list
        Operation test2 = new Filter("FILTER count>1");
        Assert.assertNull(test2.applyOp(null));

        // Test 3: No results found
        Operation test3 = new Filter("FILTER count>10");
        Assert.assertNotNull(test3.applyOp(events));

        // Test 4: filter by time
        Operation test4 = new Filter("FILTER time<2");
        List<Event> correctResult = new ArrayList<>();
        correctResult.add(new Event("10", "count", "count",dataItems));
        Assert.assertEquals(correctResult, test4.applyOp(events));

        // Test 5: filter by data item
        Operation test5 = new Filter("FILTER count<2");
        List<Event> correctResult5 = new ArrayList<>();
        correctResult5.add(new Event("5", "count", "count", dataItems));
        correctResult5.add(new Event("15", "count", "count", dataItems));
        Assert.assertEquals(correctResult5, test5.applyOp(events));
    }
}
