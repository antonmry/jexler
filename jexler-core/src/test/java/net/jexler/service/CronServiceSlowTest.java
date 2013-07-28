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
import net.jexler.test.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
public final class CronServiceSlowTest
{
	private final static long MS_1_MIN_10_SEC = 70000;
	private final static long MS_30_SEC = 30000;
	private final static long MS_10_SEC = 10000;
	private final static String CRON_EVERY_MIN = "* * * * *";

	/**
	 * Takes about 5 minutes to complete.
	 */
	@Test
    public void testEveryMinute() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	CronService service = new CronService(jexler, "cronid");
    	service.setCron(CRON_EVERY_MIN);
    	assertEquals("must be same", "cronid", service.getId());
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	assertTrue("must be true", service.waitForStartup(MS_30_SEC));
    	
    	Event event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	CronEvent cronEvent = (CronEvent)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	cronEvent = (CronEvent)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_30_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_1_MIN_10_SEC));
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	assertTrue("must be true", service.waitForStartup(MS_30_SEC));
    	
    	service.start();
    	assertTrue("must be true", service.getRunState().isIdle());

    	event = jexler.takeEvent(MS_1_MIN_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	cronEvent = (CronEvent)event;
    	assertEquals("must be same", CRON_EVERY_MIN, cronEvent.getCron());
    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_30_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_1_MIN_10_SEC));
    	
    	service.stop();
    	assertTrue("must be true", service.isOff());
	}
	
	/**
	 * Takes about a minute to complete.
	 */
	@Test
    public void testNow() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	CronService service = new CronService(jexler, "cronid");
    	service.setCron(CronService.CRON_NOW);
    	Event event = jexler.takeEvent(MS_10_SEC);
    	assertNull("must be null", event);
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	CronEvent cronEvent = (CronEvent)event;
    	assertEquals("must be same", CronService.CRON_NOW, cronEvent.getCron());
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_10_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	cronEvent = (CronEvent)event;
    	assertEquals("must be same", CronService.CRON_NOW, cronEvent.getCron());
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_10_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
	}
	
	/**
	 * Takes about a minute to complete.
	 */
	@Test
    public void testNowPlusStop() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	CronService service = new CronService(jexler, "cronid");
    	service.setCron(CronService.CRON_NOW_AND_STOP);
    	Event event = jexler.takeEvent(MS_10_SEC);
    	assertNull("must be null", event);
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	CronEvent cronEvent = (CronEvent)event;
    	assertEquals("must be same", CronService.CRON_NOW_AND_STOP, cronEvent.getCron());
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertTrue("must be true", event instanceof StopEvent);
    	    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_10_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
    	
    	service.start();
    	assertTrue("must be true", service.isOn());
    	
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertEquals("must be same", service, event.getService());
    	assertTrue("must be true", event instanceof CronEvent);
    	cronEvent = (CronEvent)event;
    	assertEquals("must be same", CronService.CRON_NOW_AND_STOP, cronEvent.getCron());
    	event = jexler.takeEvent(MS_10_SEC);
    	assertNotNull("must not be null", event);
    	assertTrue("must be true", event instanceof StopEvent);
    	
    	service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_10_SEC));
    	assertNull("must be null", jexler.takeEvent(MS_10_SEC));
	}

}
