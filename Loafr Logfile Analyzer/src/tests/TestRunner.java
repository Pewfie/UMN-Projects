package tests;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import tests.controller.ControllerPipeTests;
import tests.controller.ControllerTests;
import tests.fileutil.ConfigFileReaderTests;
import tests.fileutil.LogfileReaderTests;
import tests.fileutil.LogfileWriterTests;
import tests.fileutil.ScriptParserTests;
import tests.script.ScriptTests;
import tests.script.operation.SearchTests;
import tests.ui.UITests;

@RunWith(Suite.class)
@Suite.SuiteClasses({LoafrTests.class, UITests.class, ScriptTests.class, SearchTests.class, ConfigFileReaderTests.class,
        LogfileReaderTests.class, LogfileWriterTests.class, ScriptParserTests.class, ControllerPipeTests.class,
        ControllerTests.class})
class TestSuite {}
public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestSuite.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("RESULT: " + result.wasSuccessful());
    }
}
