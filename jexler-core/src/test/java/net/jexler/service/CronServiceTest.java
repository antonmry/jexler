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
import net.jexler.Event;
import net.jexler.VerySlowTests;
import net.jexler.impl.MockJexler;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(VerySlowTests.class)
public final class CronServiceTest
{
	private final static long MS_1_MIN_10_SEC = 70000;
	private final static String CRON_EVERY_MIN = "* * * * *";

	/**
	 * Takes about 5 minutes to complete.
	 */
	@Test
    public void testCron() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	CronService cronService = new CronService(jexler, "cronid");
    	cronService.setCron(CRON_EVERY_MIN);
    	assertEquals("must be same", "cronid", cronService.getId());
    	
    	cronService.start();
    	
    	Event event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", cronService, event.getService());
    	assertTrue("must be true", event instanceof CronService.Event);
    	CronService.Event cronEvent = (CronService.Event)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", cronService, event.getService());
    	assertTrue("must be true", event instanceof CronService.Event);
    	cronEvent = (CronService.Event)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	cronService.stop();
    	assertNull("must be null", jexler.takeEvent(MS_1_MIN_10_SEC));
    	
    	cronService.start();
    	
    	event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", cronService, event.getService());
    	assertTrue("must be true", event instanceof CronService.Event);
    	cronEvent = (CronService.Event)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	cronService.stop();
    	assertNull("must be null", jexler.takeEvent(MS_1_MIN_10_SEC));
	}
}
