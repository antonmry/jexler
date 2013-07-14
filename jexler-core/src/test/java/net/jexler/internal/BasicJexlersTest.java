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

import java.io.File;
import java.nio.file.Files;

import net.jexler.FastTests;
import net.jexler.JexlerFactory;

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
    public void getJexlerIdTest() throws Exception {
	
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
    public void getJexlerFileTest() throws Exception {
		
		File dir = Files.createTempDirectory(null).toFile();
		BasicJexlers jexlers = new BasicJexlers(dir, new JexlerFactory());

		File file = jexlers.getJexlerFile("foo");
		assertEquals("must be same",
				new File(dir, "foo.groovy").getCanonicalPath(),
				file.getCanonicalPath());
	}
	
}
