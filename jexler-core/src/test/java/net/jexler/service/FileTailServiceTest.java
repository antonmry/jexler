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

package net.jexler.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;

import net.jexler.Event;
import net.jexler.VerySlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(VerySlowTests.class)
public final class FileTailServiceTest
{
	private final static long MS_10_SEC = 10000;

	/**
	 * Takes about a minute to complete.
	 */
	@Test
    public void testFileTail() throws Exception {
    	
		File tempDir = Files.createTempDirectory(null).toFile();
    	File tempFile = new File(tempDir, "temp");
		
    	ServiceMockJexler jexler = new ServiceMockJexler();
    	FileTailService fileTailService = new FileTailService(jexler, "tailid");
    	fileTailService.setFile(tempFile);
    	fileTailService.addFilterPattern("hello[0-9]+there");
    	fileTailService.addFilterPattern("!hello55there");
    	
    	assertEquals("must be same", "tailid", fileTailService.getId());
    	
    	fileTailService.start();
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));

    	PrintStream out = new PrintStream(tempFile);
    	out.println("hello12there");
    	out.flush();
    	
    	Event event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", fileTailService, event.getService());
    	assertTrue("must be true", event instanceof FileTailService.Event);
    	FileTailService.Event fileTailEvent = (FileTailService.Event)event;
    	assertEquals("must be same", "hello12there", fileTailEvent.getLine());

    	out.println("hello33there hello44there");
    	out.println("hello there");
    	out.println("hello22there");
    	out.println("hello55there");
    	out.flush();
    	
        event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", fileTailService, event.getService());
    	assertTrue("must be true", event instanceof FileTailService.Event);
    	fileTailEvent = (FileTailService.Event)event;
    	assertEquals("must be same", "hello33there hello44there", fileTailEvent.getLine());
    	
        event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", fileTailService, event.getService());
    	assertTrue("must be true", event instanceof FileTailService.Event);
    	fileTailEvent = (FileTailService.Event)event;
    	assertEquals("must be same", "hello22there", fileTailEvent.getLine());
    	
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));

    	fileTailService.stop();

    	out.println("hello11there");
    	out.flush();

    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));

    	// exclude one more pattern
    	fileTailService.addFilterPattern("!hello22there");

    	fileTailService.start();
    	
    	// get the matching lines (previously 4, now one less)
    	assertNotNull("must not be null", jexler.takeEvent(MS_10_SEC));
    	assertNotNull("must not be null", jexler.takeEvent(MS_10_SEC));
    	assertNotNull("must not be null", jexler.takeEvent(MS_10_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	out.println("hello12there");
    	out.flush();
    	
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", fileTailService, event.getService());
    	assertTrue("must be true", event instanceof FileTailService.Event);
    	fileTailEvent = (FileTailService.Event)event;
    	assertEquals("must be same", "hello12there", fileTailEvent.getLine());
    	
    	out.close();
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	// rewrite file
    	out = new PrintStream(tempFile);
    	out.println("hello37there");
    	out.close();

    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", fileTailService, event.getService());
    	assertTrue("must be true", event instanceof FileTailService.Event);
    	fileTailEvent = (FileTailService.Event)event;
    	assertEquals("must be same", "hello37there", fileTailEvent.getLine());
    	
    	// delete file
    	assertTrue("must be true", tempFile.delete());
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	fileTailService.stop();
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));    	
	}
}
