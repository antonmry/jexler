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

package net.jexler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.jexler.test.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
public final class JexlerUtilSlowTest
{
	
	private final static long MS_10_SEC = 10000;
	private final static long MS_20_SEC = 20000;

	static private class InterruptCounter {
		public int count = 0;
	}

	/**
	 * Takes about 20 seconds to complete.
	 */
	@Test
    public void testWaitAtLeastInterupt() throws Exception {

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
		interrupter.start();

		long t0 = System.currentTimeMillis();
		JexlerUtil.waitAtLeast(MS_20_SEC);
		long t1 = System.currentTimeMillis();
		assertTrue("must be true", t1-t0 >= MS_20_SEC);
		assertTrue("should usually be true", t1-t0 < MS_20_SEC + 2000);
		assertEquals("must be same", 1, interruptCounter.count);
	}
	
}
