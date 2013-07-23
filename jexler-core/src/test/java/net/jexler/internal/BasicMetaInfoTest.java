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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import net.jexler.MetaInfo;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class BasicMetaInfoTest
{

	@Test
    public void testBasic() throws Exception {
    	
		MetaInfo info = BasicMetaInfo.EMPTY;
		assertTrue("must be true", info.isEmpty());
		
		info = new BasicMetaInfo(new File("/does/not/exist/5092749"));
		assertTrue("must be true", info.isEmpty());
		
		try {
			 new BasicMetaInfo(Files.createTempDirectory(null).toFile());
			 fail("must throw");
		} catch (IOException e) {
		}
		
		File testFile = Files.createTempFile(null, null).toFile();
        info = new BasicMetaInfo(testFile);
		assertTrue("must be true", info.isEmpty());
		
        FileWriter writer = new FileWriter(testFile);
        writer.append("// some comment");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertTrue("must be true", info.isEmpty());
		
        writer = new FileWriter(testFile);
        writer.append("import java.io.File");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertTrue("must be true", info.isEmpty());
		
        writer = new FileWriter(testFile);
        writer.append("# does not compile");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertTrue("must be true", info.isEmpty());
		
        writer = new FileWriter(testFile);
        writer.append("[ 'list', 'not', 'map' ]");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertTrue("must be true", info.isEmpty());
		
        writer = new FileWriter(testFile);
        writer.append("   [ 'autostart' : true, 'foo' : 'bar' ]");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertEquals("must be same", 2, info.size());
		assertEquals("must be same", true, info.get("autostart"));
		assertEquals("must be same", "bar", info.get("foo"));
		
		assertTrue("must be true", info.isOn("autostart", false));
		assertTrue("must be true", info.isOn("autostart", true));
		assertFalse("must be false", info.isOn("other", false));
		assertTrue("must be true", info.isOn("other", true));
		assertFalse("must be false", info.isOn("foo", false));
		assertTrue("must be true", info.isOn("foo", true));
		assertFalse("must be false", info.isOn(null, false));
		assertTrue("must be true", info.isOn(null, true));
		
        writer = new FileWriter(testFile);
        writer.append("meta = [ 'autostart' : true, 'foo' : 'bar' ]");
        writer.close();
        info = new BasicMetaInfo(testFile);
		assertEquals("must be same", 2, info.size());
	}
}
