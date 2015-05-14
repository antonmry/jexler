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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import it.sauronsoftware.cron4j.Scheduler;
import net.jexler.IssueFactory;
import net.jexler.Jexler;
import net.jexler.JexlerFactory;
import net.jexler.JexlerUtil;
import net.jexler.RunState;
import net.jexler.service.StopEvent;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class BasicJexlerContainerTest {
    private final static long MS_1_SEC = 1000;
    private final static long MS_10_SEC = 10000;

    @Test
    public void testMain() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();

        String jexlerTemplate =
                "while (true) {\n" +
                        "  event = events.take()\n" +
                        "  if (event instanceof StopEvent) {\n" +
                        "    return\n" +
                        "  }\n" +
                        "}\n";

        FileWriter writer = new FileWriter(new File(dir, "jexler1.groovy"));
        writer.append("[ 'autostart' : false ]\n");
        writer.append(jexlerTemplate);
        writer.close();

        // (note autostart true)
        writer = new FileWriter(new File(dir, "jexler2.groovy"));
        writer.append("[ 'autostart' : true ]\n");
        writer.append(jexlerTemplate);
        writer.close();

        writer = new FileWriter(new File(dir, "jexler3.groovy"));
        writer.append("[ 'autostart' : false ]\n");
        writer.append(jexlerTemplate);
        writer.close();

        writer = new FileWriter(new File(dir, "jexler4.properties"));
        writer.append("foo.bar=xyz\n");
        writer.close();

        BasicJexlerContainer container = new BasicJexlerContainer(dir, new JexlerFactory());
        assertEquals("must be same", RunState.OFF, container.getRunState());
        assertEquals("must be same", dir, container.getDir());
        assertEquals("must be same", dir.getName(), container.getId());
        assertEquals("must be same", 3, container.getJexlers().size());

        Jexler jexler1 = container.getJexler("jexler1");
        Jexler jexler2 = container.getJexler("jexler2");
        Jexler jexler3 = container.getJexler("jexler3");
        assertEquals("must be same", "jexler1", jexler1.getId());
        assertEquals("must be same", "jexler2", jexler2.getId());
        assertEquals("must be same", "jexler3", jexler3.getId());
        assertEquals("must be same", RunState.OFF, jexler1.getRunState());
        assertEquals("must be same", RunState.OFF, jexler2.getRunState());
        assertEquals("must be same", RunState.OFF, jexler3.getRunState());

        assertEquals("must be same", 0, container.getIssues().size());

        container.start();
        container.waitForStartup(MS_10_SEC);
        assertEquals("must be same", RunState.IDLE, container.getRunState());
        assertTrue("must be true", container.isOn());
        assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());

        container.stop();
        container.waitForShutdown(MS_10_SEC);
        assertEquals("must be same", RunState.OFF, container.getRunState());
        assertTrue("must be true", container.isOff());
        assertEquals("must be same", RunState.OFF, jexler1.getRunState());
        assertEquals("must be same", RunState.OFF, jexler2.getRunState());
        assertEquals("must be same", RunState.OFF, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());

        container.autostart();
        container.waitForStartup(MS_10_SEC);
        assertEquals("must be same", RunState.IDLE, container.getRunState());
        assertTrue("must be true", container.isOn());
        assertEquals("must be same", RunState.OFF, jexler1.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
        assertEquals("must be same", RunState.OFF, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());

        container.start();
        container.waitForStartup(MS_10_SEC);
        assertEquals("must be same", RunState.IDLE, container.getRunState());
        assertTrue("must be true", container.isOn());
        assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());

        jexler3.handle(new StopEvent(jexler3));
        JexlerUtil.waitAtLeast(MS_1_SEC);
        assertEquals("must be same", RunState.IDLE, container.getRunState());
        assertTrue("must be true", container.isOn());
        assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
        assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
        assertEquals("must be same", RunState.OFF, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());

        // delete file for jexler2
        assertTrue("must be true", jexler2.getFile().delete());
        assertEquals("must be same", jexler2, container.getJexler("jexler2"));
        assertEquals("must be same", 3, container.getJexlers().size());
        // don't remove running jexler even if file is gone
        container.refresh();
        assertEquals("must be same", 3, container.getJexlers().size());

        container.stop();
        container.waitForShutdown(MS_10_SEC);
        assertEquals("must be same", RunState.OFF, container.getRunState());
        assertTrue("must be true", container.isOff());
        assertEquals("must be same", RunState.OFF, jexler1.getRunState());
        assertEquals("must be same", RunState.OFF, jexler2.getRunState());
        assertEquals("must be same", RunState.OFF, jexler3.getRunState());
        assertEquals("must be same", 0, container.getIssues().size());
        assertEquals("must be same", 3, container.getJexlers().size());
        // now must remove jexler2
        container.refresh();
        assertEquals("must be same", 2, container.getJexlers().size());

        container.start();
        container.waitForStartup(MS_10_SEC);
        assertEquals("must be same", 2, container.getJexlers().size());

        assertTrue("must be true", container.getIssues().isEmpty());
        RuntimeException ex = new RuntimeException();
        container.trackIssue(null, "mock issue", ex);
        assertEquals("must be same", 1, container.getIssues().size());
        assertNull("must be null", container.getIssues().get(0).getService());
        assertEquals("must be same", "mock issue", container.getIssues().get(0).getMessage());
        assertEquals("must be same", ex, container.getIssues().get(0).getCause());
        container.forgetIssues();
        assertTrue("must be true", container.getIssues().isEmpty());

        container.trackIssue(new IssueFactory().get(null, "mock issue", ex));
        assertEquals("must be same", 1, container.getIssues().size());

        container.stop();
        container.waitForShutdown(MS_10_SEC);
    }

    @Test
    public void testConstructor() throws Exception {

        File dir = new File("does-not-exist");
        try {
            new BasicJexlerContainer(dir, new JexlerFactory());
            fail("must throw");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            assertTrue("must be true", msg.startsWith("Directory '"));
            assertTrue("must be true", msg.endsWith("does-not-exist' does not exist."));
        }

        dir = Files.createTempFile(null, ".tmp").toFile();
        try {
            new BasicJexlerContainer(dir, new JexlerFactory());
            fail("must throw");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            assertTrue("must be true", msg.startsWith("File '"));
            assertTrue("must be true", msg.endsWith(".tmp' is not a directory."));
        }
    }

    @Test
    public void testGetJexlerId() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        BasicJexlerContainer container = new BasicJexlerContainer(dir, new JexlerFactory());

        String id = container.getJexlerId(new File(dir, "foo.groovy"));
        assertEquals("must be same", "foo", id);

        id = container.getJexlerId(new File("foo.groovy"));
        assertEquals("must be same", "foo", id);

        id = container.getJexlerId(new File("foo.java"));
        assertNull("must be null", id);
    }

    @Test
    public void testGetJexlerFile() throws Exception {

        File dir = Files.createTempDirectory(null).toFile();
        BasicJexlerContainer container = new BasicJexlerContainer(dir, new JexlerFactory());

        File file = container.getJexlerFile("foo");
        assertEquals("must be same",
                new File(dir, "foo.groovy").getCanonicalPath(),
                file.getCanonicalPath());
    }

    @Test
    public void testSharedSchedulerAndClose() throws Exception {
        File dir = Files.createTempDirectory(null).toFile();
        BasicJexlerContainer container = new BasicJexlerContainer(dir, new JexlerFactory());

        Scheduler scheduler1 = container.getSharedScheduler();
        Scheduler scheduler2 = container.getSharedScheduler();
        assertEquals("must be same", scheduler1, scheduler2);
        assertTrue("must be true", scheduler1.isStarted());

        container.close();
        Scheduler scheduler3 = container.getSharedScheduler();
        assertFalse("must be false", scheduler1 == scheduler3);
        assertFalse("must be false", scheduler1.isStarted());
        assertTrue("must be true", scheduler3.isStarted());

        container.close();
        assertFalse("must be false", scheduler3.isStarted());
    }

}
