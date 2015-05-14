/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.jexler.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.jexler.test.FastTests;

import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class ShellToolTest
{

    private String testStdout = "";
    private String testStderr = "";

    @Test
    public void testDefault() throws Exception {
        ShellTool tool = new ShellTool();
        
        ShellTool.Result result;
        if (isWindows()) {
            result = tool.run("cmd /c dir");
        } else {
            result = tool.run("ls -l");
        }
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertEquals("must be same", 0, result.rc);
        assertNotNull("must not be null", result.stdout);
        assertTrue("must be true", !result.stdout.isEmpty());
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.stderr.isEmpty());
        
        if (isWindows()) {
            result = tool.run(Arrays.asList(new String[] { "cmd","/c", "dir" }));
        } else {
            result = tool.run(Arrays.asList(new String[] { "ls","-l" }));
        }
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertNotNull("must not be null", result.stdout);
        assertNotNull("must not be null", result.stderr);
        assertEquals("must be same", 0, result.rc);
        assertTrue("must be true", !result.stdout.isEmpty());        
    }

    @Test
    public void testCustom() throws Exception {
        ShellTool tool = new ShellTool();
        
        File dir = Files.createTempDirectory(null).toFile();
        File file1 = new File(dir, "file1");
        File file2 = new File(dir, "file2");
        Files.createFile(file1.toPath());
        Files.createFile(file2.toPath());
        tool.setWorkingDirectory(dir);
        
        tool.setStdoutLineHandler(new MethodClosure(this, "accumulateStdout"));
        tool.setStderrLineHandler(new MethodClosure(this, "accumulateStderr"));
        
        ShellTool.Result result;
        if (isWindows()) {
            result = tool.run("cmd /c dir");
        } else {
            result = tool.run("ls -l");
        }
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertEquals("must be same", 0, result.rc);
        assertNotNull("must not be null", result.stdout);
        assertTrue("must be true", !result.stdout.isEmpty());
        assertTrue("must be true", result.stdout.contains("file1"));
        assertTrue("must be true", result.stdout.contains("file2"));
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.stderr.isEmpty());
        
        //System.out.println("testStdout='" + testStdout + "'");
        //System.out.println("testStderr='" + testStderr + "'");
        assertTrue("must be true", !testStdout.isEmpty());
        assertTrue("must be true", testStdout.contains("file1"));
        assertTrue("must be true", testStdout.contains("file2"));
        assertTrue("must be true", testStderr.isEmpty());

        tool.setWorkingDirectory(null);
        Map<String,String> env = new HashMap<>();
        env.put("MYVAR", "there");
        tool.setEnvironment(env);
        if (isWindows()) {
            // (not case sensitive on windows)
            String[] cmd = new String[] { "cmd", "/c", "echo hello %MyVar%" };
            result = tool.run(Arrays.asList(cmd));
        } else {
            String[] cmd = new String[] { "sh", "-c", "echo hello $MYVAR" };
            result = tool.run(Arrays.asList(cmd));
        }
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertEquals("must be same", 0, result.rc);
        assertNotNull("must not be null", result.stdout);
        assertTrue("must be true", !result.stdout.isEmpty());
        assertTrue("must be true", result.stdout.contains("hello there"));
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.stderr.isEmpty());
    }

    @Test
    public void testError() throws Exception {
        ShellTool tool = new ShellTool();
        
        ShellTool.Result result;

        if (isWindows()) {
            result = tool.run("cmd /c type there-is-no-such-file");
        } else {
            result = tool.run("cat there-is-no-such-file");
        }
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertNotNull("must not be null", result.stdout);
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.rc != 0);
        assertTrue("must be true", !result.stderr.isEmpty());
        assertTrue("must be true", !result.stderr.contains("Exception"));

        result = tool.run("there-is-no-such-command");
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertNotNull("must not be null", result.stdout);
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.rc != 0);
        assertTrue("must be true", !result.stderr.isEmpty());
        System.out.println(result.stderr);
        assertTrue("must be true", result.stderr.contains("java.io.IOException"));

        result = tool.run(Arrays.asList(new String[] { "there-is-no-such-command" }));
        //System.out.println(result);
        assertNotNull("must not be null", result);
        assertNotNull("must not be null", result.stdout);
        assertNotNull("must not be null", result.stderr);
        assertTrue("must be true", result.rc != 0);
        assertTrue("must be true", !result.stderr.isEmpty());
        System.out.println(result.stderr);
        assertTrue("must be true", result.stderr.contains("java.io.IOException"));
    }

    @Test
    public void testResultToString() throws Exception {
        ShellTool tool = new ShellTool();
        ShellTool.Result result = tool.new Result();
        result.rc = 5;
        result.stdout="file1\nfile2\n";
        result.stderr="";
        //System.out.println(result);
        assertEquals("must be same", "[rc=5,stdout='file1%nfile2%n',stderr='']", result.toString());
    }

    private boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    @SuppressWarnings("unused")
    private void accumulateStdout(String line) {
        testStdout += line + ";";
    }

    @SuppressWarnings("unused")
    private void accumulateStderr(String line) {
        testStderr += line + ";";
    }

}
