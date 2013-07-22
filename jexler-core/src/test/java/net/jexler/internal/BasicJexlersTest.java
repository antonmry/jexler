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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

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
public final class BasicJexlersTest
{

	@Test
    public void testBasic() throws Exception {
	
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

		BasicJexlers jexlers = new BasicJexlers(dir, new JexlerFactory());
		assertEquals("must be same", RunState.OFF, jexlers.getRunState());
		assertEquals("must be same", dir, jexlers.getDir());
		assertEquals("must be same", dir.getName(), jexlers.getId());
		assertEquals("must be same", 3, jexlers.getJexlers().size());
		
		Jexler jexler1 = jexlers.getJexler("jexler1");
		Jexler jexler2 = jexlers.getJexler("jexler2");
		Jexler jexler3 = jexlers.getJexler("jexler3");
		assertEquals("must be same", "jexler1", jexler1.getId());
		assertEquals("must be same", "jexler2", jexler2.getId());
		assertEquals("must be same", "jexler3", jexler3.getId());
		assertEquals("must be same", RunState.OFF, jexler1.getRunState());
		assertEquals("must be same", RunState.OFF, jexler2.getRunState());
		assertEquals("must be same", RunState.OFF, jexler3.getRunState());
		
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		jexlers.start();
		jexlers.waitForStartup(10000);
		assertEquals("must be same", RunState.IDLE, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOn());
		assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		jexlers.stop();
		jexlers.waitForShutdown(10000);
		assertEquals("must be same", RunState.OFF, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOff());
		assertEquals("must be same", RunState.OFF, jexler1.getRunState());
		assertEquals("must be same", RunState.OFF, jexler2.getRunState());
		assertEquals("must be same", RunState.OFF, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		jexlers.autostart();
		jexlers.waitForStartup(10000);
		assertEquals("must be same", RunState.IDLE, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOn());
		assertEquals("must be same", RunState.OFF, jexler1.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
		assertEquals("must be same", RunState.OFF, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		jexlers.start();
		jexlers.waitForStartup(10000);
		assertEquals("must be same", RunState.IDLE, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOn());
		assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		jexler3.handle(new StopEvent(jexler3));
		JexlerUtil.waitAtLeast(1000);
		assertEquals("must be same", RunState.IDLE, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOn());
		assertEquals("must be same", RunState.IDLE, jexler1.getRunState());
		assertEquals("must be same", RunState.IDLE, jexler2.getRunState());
		assertEquals("must be same", RunState.OFF, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		
		// delete file for jexler2
		assertTrue("must be true", jexler2.getFile().delete());
		assertEquals("must be same", jexler2, jexlers.getJexler("jexler2"));
		assertEquals("must be same", 3, jexlers.getJexlers().size());
		// don't remove running jexler even if file is gone
		jexlers.refresh();
		assertEquals("must be same", 3, jexlers.getJexlers().size());

		jexlers.stop();
		jexlers.waitForShutdown(10000);
		assertEquals("must be same", RunState.OFF, jexlers.getRunState());
		assertTrue("must be true", jexlers.isOff());
		assertEquals("must be same", RunState.OFF, jexler1.getRunState());
		assertEquals("must be same", RunState.OFF, jexler2.getRunState());
		assertEquals("must be same", RunState.OFF, jexler3.getRunState());
		assertEquals("must be same", 0, jexlers.getIssues().size());
		assertEquals("must be same", 3, jexlers.getJexlers().size());
		// now must remove jexler2
		jexlers.refresh();
		assertEquals("must be same", 2, jexlers.getJexlers().size());
		
		jexlers.start();
		jexlers.waitForStartup(10000);
		assertEquals("must be same", 2, jexlers.getJexlers().size());
		
		assertTrue("must be true", jexlers.getIssues().isEmpty());
		RuntimeException ex = new RuntimeException();
		jexlers.trackIssue(null, "mock issue", ex);
		assertEquals("must be same", 1, jexlers.getIssues().size());
		assertNull("must be null", jexlers.getIssues().get(0).getService());
		assertEquals("must be same", "mock issue", jexlers.getIssues().get(0).getMessage());
		assertEquals("must be same", ex, jexlers.getIssues().get(0).getException());
		jexlers.forgetIssues();
		assertTrue("must be true", jexlers.getIssues().isEmpty());
		
		jexlers.stop();
		jexlers.waitForShutdown(10000);
	}

	@Test
    public void testGetJexlerId() throws Exception {
	
		File dir = Files.createTempDirectory(null).toFile();
		BasicJexlers jexlers = new BasicJexlers(dir, new JexlerFactory());
		
		String id = jexlers.getJexlerId(new File(dir, "foo.groovy"));
		assertEquals("must be same", "foo", id);
		
		id = jexlers.getJexlerId(new File("foo.groovy"));
		assertEquals("must be same", "foo", id);

		id = jexlers.getJexlerId(new File("foo.java"));
		assertNull("must be null", id);
	}
	
	@Test
    public void testGetJexlerFile() throws Exception {
		
		File dir = Files.createTempDirectory(null).toFile();
		BasicJexlers jexlers = new BasicJexlers(dir, new JexlerFactory());

		File file = jexlers.getJexlerFile("foo");
		assertEquals("must be same",
				new File(dir, "foo.groovy").getCanonicalPath(),
				file.getCanonicalPath());
	}
	
}
