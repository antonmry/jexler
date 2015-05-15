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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.jexler.RunState;
import net.jexler.test.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
public final class ServiceUtilSlowTest {

    private final static long MS_10_SEC = 10000;
    private final static long MS_20_SEC = 20000;
    private final static long MS_30_SEC = 30000;

    static private class InterruptCounter {
        public int count = 0;
    }

    /**
     * Takes about 30 seconds to complete.
     */
    @Test
    public void testWaitForStartup() throws Exception {

        final MockService service = new MockService(null, "mock");

        service.setRunState(RunState.BUSY_STARTING);
        assertFalse("must be false", ServiceUtil.waitForStartup(service, 0));

        final Thread current = Thread.currentThread();
        final InterruptCounter interruptCounter = new InterruptCounter();

        // interrupts waiting thread once after 10 seconds
        Thread interrupter = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(MS_10_SEC);
                } catch (InterruptedException e) {
                }
                current.interrupt();
                interruptCounter.count++;
            }
        });

        // sets service to idle after 20 seconds
        Thread starter = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(MS_20_SEC);
                } catch (InterruptedException e) {
                }
                service.setRunState(RunState.IDLE);
            }
        });

        interrupter.start();
        starter.start();
        assertTrue("must be true", ServiceUtil.waitForStartup(service, MS_30_SEC));
        assertEquals("must be same", 1, interruptCounter.count);
    }


    /**
     * Takes about 30 seconds to complete.
     */
    @Test
    public void testWaitForShutdown() throws Exception {

        final MockService service = new MockService(null, "mock");

        service.setRunState(RunState.IDLE);
        assertFalse("must be false", ServiceUtil.waitForShutdown(service, 0));

        final Thread current = Thread.currentThread();
        final InterruptCounter interruptCounter = new InterruptCounter();

        // interrupts waiting thread once after 10 seconds
        Thread interrupter = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(MS_10_SEC);
                } catch (InterruptedException e) {
                }
                current.interrupt();
                interruptCounter.count++;
            }
        });

        // sets service to off after 20 seconds
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(MS_20_SEC);
                } catch (InterruptedException e) {
                }
                service.setRunState(RunState.OFF);
            }
        });

        interrupter.start();
        stopper.start();
        assertTrue("must be true", ServiceUtil.waitForShutdown(service, MS_30_SEC));
        assertEquals("must be same", 1, interruptCounter.count);
    }

}
