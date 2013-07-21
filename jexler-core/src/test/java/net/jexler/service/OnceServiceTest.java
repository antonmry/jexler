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
import net.jexler.internal.MockJexler;
import net.jexler.test.VerySlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(VerySlowTests.class)
public final class OnceServiceTest
{
	private final static long MS_20_SEC = 20000;

	/**
	 * Takes about a minute to complete.
	 */
	@Test
    public void testCron() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	OnceService service = new OnceService(jexler, "onceid");
    	Event event = jexler.takeEvent(MS_20_SEC);
    	assertNull("must be null", event);
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_20_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof OnceEvent);
    	    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_20_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_20_SEC));
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_20_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof OnceEvent);
    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_20_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_20_SEC));
	}
}
