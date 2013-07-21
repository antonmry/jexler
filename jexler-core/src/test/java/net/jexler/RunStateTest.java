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
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class RunStateTest
{
	
	@Test
    public void testBasic() throws Exception {
		assertTrue("must be true", RunState.OFF.isOff());
		assertTrue("must be true", !RunState.OFF.isOn());
		assertTrue("must be true", !RunState.OFF.isOperational());
		assertTrue("must be true", !RunState.OFF.isBusyStarting());
		assertTrue("must be true", !RunState.OFF.isIdle());
		assertTrue("must be true", !RunState.OFF.isBusyEvent());
		assertTrue("must be true", !RunState.OFF.isBusyStopping());
		
		assertTrue("must be true", !RunState.BUSY_STARTING.isOff());
		assertTrue("must be true", RunState.BUSY_STARTING.isOn());
		assertTrue("must be true", !RunState.BUSY_STARTING.isOperational());
		assertTrue("must be true", RunState.BUSY_STARTING.isBusyStarting());
		assertTrue("must be true", !RunState.BUSY_STARTING.isIdle());
		assertTrue("must be true", !RunState.BUSY_STARTING.isBusyEvent());
		assertTrue("must be true", !RunState.BUSY_STARTING.isBusyStopping());
		
		assertTrue("must be true", !RunState.IDLE.isOff());
		assertTrue("must be true", RunState.IDLE.isOn());
		assertTrue("must be true", RunState.IDLE.isOperational());
		assertTrue("must be true", !RunState.IDLE.isBusyStarting());
		assertTrue("must be true", RunState.IDLE.isIdle());
		assertTrue("must be true", !RunState.IDLE.isBusyEvent());
		assertTrue("must be true", !RunState.IDLE.isBusyStopping());
		
		assertTrue("must be true", !RunState.BUSY_EVENT.isOff());
		assertTrue("must be true", RunState.BUSY_EVENT.isOn());
		assertTrue("must be true", RunState.BUSY_EVENT.isOperational());
		assertTrue("must be true", !RunState.BUSY_EVENT.isBusyStarting());
		assertTrue("must be true", !RunState.BUSY_EVENT.isIdle());
		assertTrue("must be true", RunState.BUSY_EVENT.isBusyEvent());
		assertTrue("must be true", !RunState.BUSY_EVENT.isBusyStopping());
		
		assertTrue("must be true", !RunState.BUSY_STOPPING.isOff());
		assertTrue("must be true", RunState.BUSY_STOPPING.isOn());
		assertTrue("must be true", !RunState.BUSY_STOPPING.isOperational());
		assertTrue("must be true", !RunState.BUSY_STOPPING.isBusyStarting());
		assertTrue("must be true", !RunState.BUSY_STOPPING.isIdle());
		assertTrue("must be true", !RunState.BUSY_STOPPING.isBusyEvent());
		assertTrue("must be true", RunState.BUSY_STOPPING.isBusyStopping());
		
		assertEquals("must be equal", "off", RunState.OFF.getInfo());
		assertEquals("must be equal", RunState.OFF, RunState.valueOf("OFF"));
		assertEquals("must be equal", RunState.OFF, RunState.values()[0]);
	}
	
}
