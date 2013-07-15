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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import net.jexler.JexlerFactory;
import net.jexler.JexlerUtil;
import net.jexler.RunState;
import net.jexler.service.MockService;
import net.jexler.test.FastTests;

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
		File file = new File(dir, "test.groovy");
		
		FileWriter writer = new FileWriter(file);
		writer.append("");
		writer.close();
		
		BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
		assertEquals("must be same", file.getAbsolutePath(), jexler.getFile().getAbsolutePath());
		assertEquals("must be same", dir.getAbsolutePath(), jexler.getDir().getAbsolutePath());
		assertEquals("must be same", "test", jexler.getId());
		assertEquals("must be same", RunState.OFF, jexler.getRunState());
		assertTrue("must be true", jexler.getMetaInfo().isEmpty());
		assertTrue("must be true", jexler.getIssues().isEmpty());
		
		jexler.start();
		jexler.waitForStartup(10000);
		assertEquals("must be same", RunState.OFF, jexler.getRunState());
		assertTrue("must be true", jexler.getIssues().isEmpty());
	}

	@Test
    public void testSimpleJexlerScript() throws Exception {

		File dir = Files.createTempDirectory(null).toFile();
		File file = new File(dir, "test.groovy");
		
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
		
		BasicJexler jexler = new BasicJexler(file, new BasicJexlers(dir, new JexlerFactory()));
		assertEquals("must be same", RunState.OFF, jexler.getRunState());
		assertEquals("must be same", 2, jexler.getMetaInfo().size());
		assertFalse("must be false", jexler.getMetaInfo().isOn("autostart", true));
		assertFalse("must be false", jexler.getMetaInfo().isOn("autostart", false));
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
		assertEquals("must be same", ex, jexler.getIssues().get(0).getException());
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
	}

}
