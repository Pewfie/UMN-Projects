package tests.script.operation;

import main.logfile.DataItem;
import main.logfile.Event;
import main.script.operation.Operation;
import main.script.operation.Search;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SearchTests {
    @Test
    public void testApplyOp() {
        List<Event> events = new ArrayList<>();
        List<DataItem> items = new ArrayList<>();
        items.add(new DataItem("boolean", "true"));

        events.add(new Event("0","", "start", items));
        events.add(new Event("1", "", "thermometer",items));
        events.add(new Event("5", "", "count",items));
        events.add(new Event("4", "", "switch",items));
        events.add(new Event("10", "", "count",items));
        events.add(new Event("4", "", "voltage",items));
        events.add(new Event("15", "", "count",items));

        // Test 1: Operation length less than 10
        Operation testOp1 = new Search("");
        Assert.assertNull(testOp1.applyOp(null));

        // Test 2: Operation doesn't contain SEARCH
        Operation testOp2 = new Search("this is a test input");
        Assert.assertNull(testOp2.applyOp(null));

        // Test 3: null events list
        Operation testOp3 = new Search("SEARCH t");
        Assert.assertNull(testOp3.applyOp(null));

        // Test 4: No results found
        Operation testOp4 = new Search("SEARCH foo");
        Assert.assertNotNull(testOp4.applyOp(events));

        // Test 5: 1 result
        Operation testOp5 = new Search("SEARCH thermometer");
        Assert.assertNotNull(testOp5.applyOp(events));

        // Test 6: More than 1 result
        Operation testOp6 = new Search("SEARCH count");
        Assert.assertNotNull(testOp6.applyOp(events));

        // Test 7: Context before wrong format
        Operation testOp7 = new Search("SEARCH count BEFORE ");
        Assert.assertNull(testOp7.applyOp(events));

        // Test 8: Context more than one before
        Operation testOp8 = new Search("SEARCH 1 BEFORE 2 BEFORE 3");
        Assert.assertNull(testOp8.applyOp(events));

        // Test 9: Context before success
        Operation testOp9 = new Search("SEARCH count BEFORE voltage");
        Assert.assertNotNull(testOp9.applyOp(events));

        // Test 10: Context after wrong format
        Operation testOp10 = new Search("SEARCH count AFTER ");
        Assert.assertNull(testOp10.applyOp(events));

        // Test 11: Context more than one after
        Operation testOp11 = new Search("SEARCH 1 AFTER 2 AFTER 3");
        Assert.assertNull(testOp11.applyOp(events));

        // Test 12: Context before success
        Operation testOp12 = new Search("SEARCH count AFTER switch");
        Assert.assertNotNull(testOp12.applyOp(events));

        // Test 13: Context between wrong format
        Operation testOp13 = new Search("SEARCH count BETWEEN switch");
        Assert.assertNull(testOp13.applyOp(events));

        // Test 14: Context more than one between
        Operation testOp14 = new Search("SEARCH 1 BETWEEN 2 BETWEEN 3");
        Assert.assertNull(testOp14.applyOp(events));

        // Test 15: Context between success
        Operation testOp15 = new Search("SEARCH count BETWEEN thermometer AND voltage");
        Assert.assertNotNull(testOp15.applyOp(events));
    }
}
