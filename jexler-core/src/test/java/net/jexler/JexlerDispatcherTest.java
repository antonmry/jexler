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

package net.jexler;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import net.jexler.internal.BasicJexler;
import net.jexler.internal.BasicJexlers;
import net.jexler.service.MockService;
import net.jexler.service.ServiceUtil;
import net.jexler.test.FastTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class JexlerDispatcherTest
{

	@Test
    public void testNoInstance() throws Exception {
		try {
			new JexlerDispatcher();
			fail("must throw");
		} catch (JexlerDispatcher.NoInstanceException e) {
		}
	}

    @Test
    public void testMinimalMethodsAndNoSuitableHandler() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "mockService = MockService.getTestInstance()\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
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
        assertEquals("must be same", 0, mockService.getNEventsGotBack());
        assertEquals("must be same", 1, jexler.getIssues().size());
        assertEquals("must be same",
                "Dispatch: No handler for event MockEvent from service MockService.",
                jexler.getIssues().get(0).getMessage());
        assertEquals("must be same", jexler, jexler.getIssues().get(0).getService());
        assertNull("must null", jexler.getIssues().get(0).getCause());
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
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testNoStartMethod() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "mockService = MockService.getTestInstance()\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void noStartMethod() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        //System.out.println(issue);
        assertEquals("must be same", "Dispatch: Mandatory start() method missing.", issue.getMessage());
        assertEquals("must be same", jexler, issue.getService());
        assertNull("must be null", issue.getCause());
    }

    public static class TestState {
        public boolean declareCalled;
        public boolean startCalled;
        public boolean handleByClassNameAndServiceIdCalled;
        public boolean handleByClassNameCalled;
        public boolean handleCalled;
        public boolean stopCalled;
    }

    private static TestState testStateInstance;

    public static void setTestStateInstance() {
        testStateInstance = new TestState();
    }

    public static TestState getTestStateInstance() {
        return testStateInstance;
    }

    @Test
    public void testAllMethodsHandlerWithClassNameAndServiceId() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void declare() {\n" +
                "  mockService = MockService.getTestInstance()\n" +
                "  testState = JexlerDispatcherTest.getTestStateInstance()\n" +
                "  testState.declareCalled = true\n" +
                "}\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "  testState.startCalled = true\n" +
                "}\n" +
                "void handleMockEventMockService(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleByClassNameAndServiceIdCalled = true\n" +
                "}\n" +
                "void stop() {\n" +
                "  testState.stopCalled = true\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
        MockService mockService = MockService.getTestInstance();
        setTestStateInstance();
        TestState testState = getTestStateInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

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

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertTrue("must be true", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testAllMethodsHandlerWithClassName() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void declare() {\n" +
                "  mockService = MockService.getTestInstance()\n" +
                "  testState = JexlerDispatcherTest.getTestStateInstance()\n" +
                "  testState.declareCalled = true\n" +
                "}\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "  testState.startCalled = true\n" +
                "}\n" +
                "void handleMockEvent(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleByClassNameCalled = true\n" +
                "}\n" +
                "void stop() {\n" +
                "  testState.stopCalled = true\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
        MockService mockService = MockService.getTestInstance();
        setTestStateInstance();
        TestState testState = getTestStateInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

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

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertTrue("must be true", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertTrue("must be true", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertTrue("must be true", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testAllMethodsHandlerDefault() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void declare() {\n" +
                "  mockService = MockService.getTestInstance()\n" +
                "  testState = JexlerDispatcherTest.getTestStateInstance()\n" +
                "  testState.declareCalled = true\n" +
                "}\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "  testState.startCalled = true\n" +
                "}\n" +
                "void handle(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleCalled = true\n" +
                "}\n" +
                "void stop() {\n" +
                "  testState.stopCalled = true\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
        MockService mockService = MockService.getTestInstance();
        setTestStateInstance();
        TestState testState = getTestStateInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

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

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertTrue("must be true", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertTrue("must be true", testState.handleCalled);
        assertTrue("must be true", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testAllMethodsAllHandlers() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void declare() {\n" +
                "  mockService = MockService.getTestInstance()\n" +
                "  testState = JexlerDispatcherTest.getTestStateInstance()\n" +
                "  testState.declareCalled = true\n" +
                "}\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "  testState.startCalled = true\n" +
                "}\n" +
                "void handleMockEventMockService(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleByClassNameAndServiceIdCalled = true\n" +
                "}\n" +
                "void handleMockEvent(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleByClassNameCalled = true\n" +
                "}\n" +
                "void handle(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleCalled = true\n" +
                "}\n" +
                "void stop() {\n" +
                "  testState.stopCalled = true\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
        MockService mockService = MockService.getTestInstance();
        setTestStateInstance();
        TestState testState = getTestStateInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

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

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertTrue("must be true", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testHandleThrows() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "Test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("//\n" +
                "JexlerDispatcher.dispatch(this)\n" +
                "void declare() {\n" +
                "  mockService = MockService.getTestInstance()\n" +
                "  testState = JexlerDispatcherTest.getTestStateInstance()\n" +
                "  testState.declareCalled = true\n" +
                "}\n" +
                "void start() {\n" +
                "  services.add(mockService)\n" +
                "  services.start()\n" +
                "  testState.startCalled = true\n" +
                "}\n" +
                "void handleMockEventMockService(def event) {\n" +
                "  mockService.notifyGotEvent()\n" +
                "  testState.handleByClassNameAndServiceIdCalled = true\n" +
                "  throw new RuntimeException('handle failed')\n" +
                "}\n" +
                "void stop() {\n" +
                "  testState.stopCalled = true\n" +
                "}\n");
        writer.close();

        BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertEquals("must be same", 0, jexler.getMetaInfo().size());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        MockService.setTestInstance(jexler, "MockService");
        MockService mockService = MockService.getTestInstance();
        setTestStateInstance();
        TestState testState = getTestStateInstance();

        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.IDLE, jexler.getRunState());
        assertTrue("must be true", jexler.isOn());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 0, mockService.getNStopped());
        assertEquals("must be same", 0, mockService.getNEventsSent());
        assertEquals("must be same", 0, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertFalse("must be false", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

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

        assertEquals("must be same", 1, jexler.getIssues().size());
        assertEquals("must be same",
                "Dispatch: Handler handleMockEventMockService failed.",
                jexler.getIssues().get(0).getMessage());
        assertEquals("must be same", jexler, jexler.getIssues().get(0).getService());
        Throwable cause = jexler.getIssues().get(0).getCause();
        assertNotNull("must not be null", cause);
        assertTrue("must be true", cause instanceof RuntimeException);
        assertEquals("must be same", "handle failed", cause.getMessage());
        jexler.forgetIssues();
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertFalse("must be false", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());

        assertEquals("must be same", 1, mockService.getNStarted());
        assertEquals("must be same", 1, mockService.getNStopped());
        assertEquals("must be same", 1, mockService.getNEventsSent());
        assertEquals("must be same", 1, mockService.getNEventsGotBack());

        assertTrue("must be true", testState.declareCalled);
        assertTrue("must be true", testState.startCalled);
        assertTrue("must be true", testState.handleByClassNameAndServiceIdCalled);
        assertFalse("must be false", testState.handleByClassNameCalled);
        assertFalse("must be false", testState.handleCalled);
        assertTrue("must be true", testState.stopCalled);

        jexler.stop();
        jexler.waitForShutdown(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.isOff());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

}
