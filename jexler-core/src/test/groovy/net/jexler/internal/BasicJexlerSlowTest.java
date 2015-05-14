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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import net.jexler.Issue;
import net.jexler.JexlerFactory;
import net.jexler.test.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
public final class BasicJexlerSlowTest
{
	private final static long MS_5_SEC = 5000;
	private final static long MS_20_SEC = 20000;

	@Test
    public void testStartShutdownTooSlow() throws Exception {
	
		File dir = Files.createTempDirectory(null).toFile();
		File file = new File(dir, "test.groovy");
		
		String jexlerTemplateSlow =
			    "log.info('before startup wait ' + jexler.id)\n" +
				"JexlerUtil.waitAtLeast(10000)\n" +
			    "log.info('after startup wait ' + jexler.id)\n" +
			    "while (true) {\n" +
				"  event = events.take()\n" +
				"  if (event instanceof StopEvent) {\n" +
				"    JexlerUtil.waitAtLeast(10000)\n" +
				"    return\n" +
				"  }\n" +
				"}\n";
		
		FileWriter writer = new FileWriter(file);
		writer.append("[ 'autostart' : false ]\n");
		writer.append(jexlerTemplateSlow);
		writer.close();
				
		BasicJexler jexler = new BasicJexler(file, new BasicJexlerContainer(dir, new JexlerFactory()));
		
		jexler.start();
		jexler.waitForStartup(MS_5_SEC);
		assertEquals("should be same", 1, jexler.getIssues().size());
		Issue issue = jexler.getIssues().get(0);
		assertEquals("must be same", "Timeout waiting for jexler startup.", issue.getMessage());
		
		jexler.forgetIssues();
		jexler.waitForStartup(MS_20_SEC);
		assertTrue("must be true", jexler.getIssues().isEmpty());
		
		jexler.stop();
		jexler.waitForShutdown(MS_5_SEC);
		assertEquals("should be same", 1, jexler.getIssues().size());
		issue = jexler.getIssues().get(0);
		assertEquals("must be same", "Timeout waiting for jexler shutdown.", issue.getMessage());
		
		jexler.forgetIssues();
		jexler.waitForShutdown(MS_20_SEC);
		assertTrue("must be true", jexler.getIssues().isEmpty());
	}
	
}
