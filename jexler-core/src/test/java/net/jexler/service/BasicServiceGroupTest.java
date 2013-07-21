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
import static org.junit.Assert.fail;
import net.jexler.RunState;
import net.jexler.internal.MockJexler;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class BasicServiceGroupTest
{

	@Test
    public void testBasic() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	MockService service1 = new MockService(jexler, "service1");
    	MockService service2 = new MockService(jexler, "service2");
    	MockService service3 = new MockService(jexler, "service2");
    	
    	BasicServiceGroup group = new BasicServiceGroup("group");
    	assertEquals("must be same", "group", group.getId());
    	assertEquals("must be same", 0, group.getServices().size());
    	
    	group.add(service1);
    	group.add(service2);
    	group.add(service3);
    	assertEquals("must be same", 3, group.getServices().size());
    	assertEquals("must be same", RunState.OFF, group.getRunState());
    	
    	service2.setRunState(RunState.BUSY_STARTING);
    	assertEquals("must be same", RunState.BUSY_STARTING, group.getRunState());
    	
    	service1.setRunState(RunState.IDLE);
    	service3.setRunState(RunState.BUSY_EVENT);
    	assertEquals("must be same", RunState.BUSY_STARTING, group.getRunState());
    	
    	service2.setRunState(RunState.IDLE);
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	
    	service1.setRunState(RunState.BUSY_STOPPING);
    	service2.setRunState(RunState.BUSY_STOPPING);
    	service3.setRunState(RunState.BUSY_STOPPING);
    	assertEquals("must be same", RunState.BUSY_STOPPING, group.getRunState());
	}

	@Test
    public void testStartStop() throws Exception {
    	
    	MockJexler jexler = new MockJexler();
    	MockService service1 = new MockService(jexler, "service1");
    	MockService service2 = new MockService(jexler, "service2");
    	MockService service3 = new MockService(jexler, "service2");
    	
    	BasicServiceGroup group = new BasicServiceGroup("group");
    	group.add(service1);
    	group.add(service2);
    	group.add(service3);
    	
    	assertEquals("must be same", RunState.OFF, group.getRunState());
    	assertFalse("must be false", group.isOn());
    	
    	service1.start();
    	assertEquals("must be same", RunState.IDLE, service1.getRunState());
    	assertEquals("must be same", RunState.OFF, service2.getRunState());
    	assertEquals("must be same", RunState.OFF, service3.getRunState());
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	assertTrue("must be true", group.isOn());
    	
    	group.start();
    	assertEquals("must be same", RunState.IDLE, service1.getRunState());
    	assertEquals("must be same", RunState.IDLE, service2.getRunState());
    	assertEquals("must be same", RunState.IDLE, service3.getRunState());
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	assertTrue("must be true", group.isOn());
    	
    	service3.stop();
    	assertEquals("must be same", RunState.IDLE, service1.getRunState());
    	assertEquals("must be same", RunState.IDLE, service2.getRunState());
    	assertEquals("must be same", RunState.OFF, service3.getRunState());
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	assertTrue("must be true", group.isOn());
    	
    	group.stop();
    	assertEquals("must be same", RunState.OFF, service1.getRunState());
    	assertEquals("must be same", RunState.OFF, service2.getRunState());
    	assertEquals("must be same", RunState.OFF, service3.getRunState());
    	assertEquals("must be same", RunState.OFF, group.getRunState());
    	assertFalse("must be false", group.isOn());
    	
    	// test runtime exception during stop, must still try to stop all
    	
    	group.start();
    	RuntimeException ex = new RuntimeException();
    	service2.setStopRuntimeException(ex);
    	assertEquals("must be same", RunState.IDLE, service1.getRunState());
    	assertEquals("must be same", RunState.IDLE, service2.getRunState());
    	assertEquals("must be same", RunState.IDLE, service3.getRunState());
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	assertTrue("must be true", group.isOn());
    	
    	try {
    		group.stop();
    		fail("must throw");
    	} catch (RuntimeException e) {
    		assertEquals("must be same", ex, e);
    	}
    	assertEquals("must be same", RunState.OFF, service1.getRunState());
    	assertEquals("must be same", RunState.IDLE, service2.getRunState());
    	assertEquals("must be same", RunState.OFF, service3.getRunState());
    	assertEquals("must be same", RunState.IDLE, group.getRunState());
    	assertTrue("must be true", group.isOn());
	}
	
}
