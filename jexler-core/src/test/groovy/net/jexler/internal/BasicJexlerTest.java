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

package net.jexler.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovy.grape.GrapeEngine;
import groovy.lang.GroovyClassLoader;
import net.jexler.Issue;
import net.jexler.JexlerFactory;
import net.jexler.JexlerUtil;
import net.jexler.RunState;
import net.jexler.service.MockService;
import net.jexler.test.FastTests;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class BasicJexlerTest
{

    @Test
    public void testEmptyJexlerScript() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        Files.createFile(file.toPath());

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));
        assertEquals("must be same", file.getAbsolutePath(), jexler.getFile().getAbsolutePath());
        assertEquals("must be same", dir.getAbsolutePath(), jexler.getDir().getAbsolutePath());
        assertEquals("must be same", "Test", jexler.getId());
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.getMetaInfo().isEmpty());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testJexlerScriptCompileFails() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");
        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "# does not compile...\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Script compile failed.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof CompilationFailedException);
    }

    @Test
    public void testJexlerScriptNotAScript() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Util.groovy");
        FileWriter writer = new FileWriter(file);
        // not an instance of java.lang.Script
        writer.append("class Util {}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getIssues().size());
    }

    @Test
    public void testJexlerScriptCreateFails() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");
        FileWriter writer = new FileWriter(file);
        writer.append("public class Test extends Script { \n" +
                "  static { throw new RuntimeException() }\n" +
                "  public def run() {}\n" +
                "}");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Script create failed.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof ExceptionInInitializerError);
    }

    @Test
    public void testJexlerScriptRunFailsWithRuntimeException() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");
        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "throw new IllegalArgumentException()\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Script run failed.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void testJexlerScriptRunFailsWithCheckedException() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");
        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "throw new FileNotFoundException()\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Script run failed.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof IOException);
    }

    @Test
    public void testJexlerScriptRunFailsWithError() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");
        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "throw new NoClassDefFoundError()\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Script run failed.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof NoClassDefFoundError);
    }

    @Test
    public void testSimpleJexlerScript() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "def mockService = MockService.getTestInstance()\n" +
                "services.add(mockService)\n" +
                "services.start()\n" +
                "while (true) {\n" +
                "  event = events.take()\n" +
                "  if (event instanceof MockEvent) {\n" +
                "    mockService.notifyGotEvent()" +
                "  } else if (event instanceof StopEvent) {\n" +
                "    return\n" +
                "  }\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 2, jexler.getMetaInfo().size());
        assertFalse("must be false", JexlerUtil.isMetaInfoOn(jexler.getMetaInfo(), "autostart", true));
        assertFalse("must be false", JexlerUtil.isMetaInfoOn(jexler.getMetaInfo(), "autostart", false));
        assertEquals("must be same", "bar", jexler.getMetaInfo().get("foo"));
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "mock-service");
        MockService mockService = MockService.getTestInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        mockService.notifyJexler();
        JexlerUtil.waitAtLeast(1000);
        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", jexler.getIssues().isEmpty());

        RuntimeException ex = new RuntimeException();
        jexler.trackIssue(mockService, "mock issue", ex);
        assertEquals("must be same", 1, jexler.getIssues().size());
        assertEquals("must be same", mockService, jexler.getIssues().get(0).getService());
        assertEquals("must be same", "mock issue", jexler.getIssues().get(0).getMessage());
        assertEquals("must be same", ex, jexler.getIssues().get(0).getCause());
        jexler.forgetIssues();
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.trackIssue(new Issue(mockService, "mock issue", ex));
        assertEquals("must be same", 1, jexler.getIssues().size());
        assertEquals("must be same", mockService, jexler.getIssues().get(0).getService());
        assertEquals("must be same", "mock issue", jexler.getIssues().get(0).getMessage());
        assertEquals("must be same", ex, jexler.getIssues().get(0).getCause());
        jexler.forgetIssues();
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testShutdownRuntimeException() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("[ 'autostart' : false, 'foo' : 'bar' ]\n" +
                "def mockService = MockService.getTestInstance()\n" +
                "mockService.setStopRuntimeException(new RuntimeException())\n" +
                "services.add(mockService)\n" +
                "services.start()\n" +
                "while (true) {\n" +
                "  event = events.take()\n" +
                "  if (event instanceof MockEvent) {\n" +
                "    mockService.notifyGotEvent()" +
                "  } else if (event instanceof StopEvent) {\n" +
                "    return\n" +
                "  }\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));

        MockService.setTestInstance(jexler, "mock-service");
        MockService mockService = MockService.getTestInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());

        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", "Could not stop services.", issue.getMessage());
        assertNotNull("must not be null", issue.getService());
        assertEquals("must be same", mockService.getStopRuntimeException(), issue.getCause());
    }

    @Test
    public void testMetaInfoIOException() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        //File file = new File(dir, "test.groovy");

        // note that passing dir as jexler file!
        BasicJexler jexler = new BasicJexler(dir, new BasicJexlerContainer(dir, new JexlerFactory()));
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.getMetaInfo();

        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertTrue("must be true",
                issue.getMessage().startsWith("Could not read meta info from jexler file"));
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof IOException);

        jexler.forgetIssues();
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.start();

        assertEquals("must be same", 1, jexler.getIssues().size());
        issue = jexler.getIssues().get(0);
        assertTrue("must be true",
                issue.getMessage().startsWith("Could not read meta info from jexler file"));
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof IOException);
    }

    @Test
    public void testEventTake() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append(
                "[ 'autoimport' : false ]\n" +
                "while (true) {\n" +
                "  event = events.take()\n" +
                "  if (event instanceof net.jexler.service.StopEvent) {\n" +
                "    return\n" +
                "  }\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));
        jexler.start();
        jexler.waitForStartup(10000);
        assertTrue("must be true", jexler.getIssues().isEmpty());

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread scriptThread = null;
        for (Thread thread : threadSet) {
            if ("Test".equals(thread.getName())) {
                scriptThread = thread;
            }
        }
        assertNotNull("must not be null", scriptThread);

        scriptThread.interrupt();

        JexlerUtil.waitAtLeast(500);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", jexler, issue.getService());
        assertEquals("must be same", "Could not take event.", issue.getMessage());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof InterruptedException);

        jexler.stop();
        jexler.waitForShutdown(10000);
    }

    private static class MockEngine implements GrapeEngine {
        @Override public Object grab(String endorsedModule) { return null; }
        @Override public Object grab(Map args) { return null; }
        @Override public Object grab(Map args, Map... dependencies) { return null; }
        @Override public Map<String, Map<String, List<String>>> enumerateGrapes() { return null; }
        @Override public URI[] resolve(Map args, Map... dependencies) { return null; }
        @Override public URI[] resolve(Map args, List depsInfo, Map... dependencies) { return null; }
        @Override public Map[] listDependencies(ClassLoader classLoader) { return null; }
        @Override public void addResolver(Map<String, Object> args) {}
    }

    @Test
    public void shallowTestOfWrappingGrapeEngine() throws Exception {
        final BasicJexler.WorkaroundGroovy7407WrappingGrapeEngine engine =
                new BasicJexler.WorkaroundGroovy7407WrappingGrapeEngine("lock", new MockEngine());
        final Map<String,Object> testMap = new HashMap<>();
        testMap.put("calleeDepth", 3);
        assertNull("must be null", engine.grab("dummy endorsed"));
        assertNull("must be null", engine.grab(new HashMap()));
        assertNull("must be null", engine.grab(new HashMap(), new HashMap()));
        assertNull("must be null", engine.grab(testMap));
        assertNull("must be null", engine.grab(testMap, testMap));
        assertNull("must be null", engine.enumerateGrapes());
        assertNull("must be null", engine.resolve(new HashMap(), new HashMap()));
        assertNull("must be null", engine.resolve(testMap, new HashMap()));
        assertNull("must be null", engine.resolve(new HashMap(), new LinkedList(), new HashMap()));
        assertNull("must be null", engine.listDependencies(new GroovyClassLoader()));
        engine.addResolver(new HashMap<String, Object>());
    }

}
