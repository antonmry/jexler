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

import net.jexler.FastTests;
import net.jexler.JexlerFactory;
import net.jexler.RunState;

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
	
}
