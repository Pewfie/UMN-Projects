package tests.fileutil;

import main.fileutil.ScriptParser;
import main.script.Script;
import main.script.operation.Operation;
import org.junit.Test;

import java.io.*;
import java.util.Objects;

import org.junit.Assert;

public class ScriptParserTests {

    @Test
    public void testParseScript() {
        // Create a temporary script file with sample data
        // Note that sample data only contains one line for now as this is the only script functionality implemented thus far
        File tempFile = createTempScriptFile("Line 1\n");

        ScriptParser scriptParser = new ScriptParser();

        // Parse the script file
        Script script = scriptParser.parseScript(tempFile);

        // Test 1: correctly parse script
        Assert.assertNotNull(script);
        String expectedLine = "Line 1";
        for (Operation op : script.getOps()) {
            Assert.assertEquals(expectedLine, op.getOp());
        }


        // Test 2: empty script file
        File emptyFile = createTempScriptFile("");
        Script emptyScript = scriptParser.parseScript(emptyFile);
        Assert.assertNotNull(emptyScript);
        Assert.assertNull(emptyScript.getOps());

        // Test 3: non-existing file
        File nonExistingFile = new File("nonExistingFile.txt");
        Script nonExistingScript = scriptParser.parseScript(nonExistingFile);
        Assert.assertNull(nonExistingScript);
    }

    private File createTempScriptFile(String content) {
        try {
            File tempFile = File.createTempFile("tempScript", ".txt");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(content);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp script file", e);
        }
    }
}
