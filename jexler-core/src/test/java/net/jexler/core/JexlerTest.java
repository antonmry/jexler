/*
   Copyright 2012 $(whois jexler.net)

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

package net.jexler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class JexlerTest {

    @Test
    public void testMock1() {
        // Simple mock that processes no messages
        Jexler jexler = JexlerFactory.getJexler(new File("src/test/testjexlers/mock1"));
        assertFalse("must be false", jexler.isRunning());

        MockHandler.clearCallList();
        jexler.start();
        assertTrue("must be true", jexler.isRunning());
        List<JexlerHandler> handlers = jexler.getHandlers();
        assertEquals("must be equal", 2, handlers.size());
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equal", 2, callList.size());
        assertEquals("must be equal", "startup : nil", callList.get(0));
        assertEquals("must be equal", "startup : nil", callList.get(1));

        MockHandler.clearCallList();
        jexler.stop();
        assertFalse("must be false", jexler.isRunning());
        assertEquals("must be equal", 0, jexler.getHandlers().size());
        callList = MockHandler.getCallList();
        assertEquals("must be equal", 2, callList.size());
        assertEquals("must be equal", "shutdown : nil", callList.get(0));
        assertEquals("must be equal", "shutdown : nil", callList.get(1));
    }

    @Test
    public void testMock2() {
        // Throws at startup and shutdown
        Jexler jexler = JexlerFactory.getJexler(new File("src/test/testjexlers/mock2"));
        assertFalse("must be false", jexler.isRunning());

        MockHandler.clearCallList();
        jexler.start();
        assertFalse("must be false", jexler.isRunning());
        List<JexlerHandler> handlers = jexler.getHandlers();
        assertEquals("must be equal", 0, handlers.size());
        //MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equal", 5, callList.size());
        assertEquals("must be equal", "startup : nil", callList.get(0));
        assertEquals("must be equal", "startup : nil", callList.get(1));
        assertEquals("must be equal", "startup : throw", callList.get(2));
        assertEquals("must be equal", "shutdown : nil", callList.get(3));
        assertEquals("must be equal", "shutdown : throw", callList.get(4));
    }

    @Test
    public void testMock3() {
        // Throws at shutdown
        Jexler jexler = JexlerFactory.getJexler(new File("src/test/testjexlers/mock3"));
        assertFalse("must be false", jexler.isRunning());

        MockHandler.clearCallList();
        jexler.start();
        assertTrue("must be true", jexler.isRunning());
        List<JexlerHandler> handlers = jexler.getHandlers();
        assertEquals("must be equal", 3, handlers.size());

        jexler.stop();
        assertFalse("must be false", jexler.isRunning());
        handlers = jexler.getHandlers();
        assertEquals("must be equal", 0, handlers.size());
        //MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equal", 6, callList.size());
        assertEquals("must be equal", "startup : nil", callList.get(0));
        assertEquals("must be equal", "startup : nil", callList.get(1));
        assertEquals("must be equal", "startup : nil", callList.get(2));
        assertEquals("must be equal", "shutdown : nil", callList.get(3));
        assertEquals("must be equal", "shutdown : throw", callList.get(4));
        assertEquals("must be equal", "shutdown : nil", callList.get(5));
    }

    @Test
    public void testMock4() {
        // Submits and handles
        Jexler jexler = JexlerFactory.getJexler(new File("src/test/testjexlers/mock4"));
        assertFalse("must be false", jexler.isRunning());

        MockHandler.clearCallList();
        jexler.start();
        assertTrue("must be true", jexler.isRunning());
        List<JexlerHandler> handlers = jexler.getHandlers();
        assertEquals("must be equal", 3, handlers.size());

        // waiting a bit because messages handled in separate thread
        // TODO better way? (wait for entries in call list plus timeout?)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }

        jexler.stop();
        assertFalse("must be false", jexler.isRunning());
        handlers = jexler.getHandlers();
        assertEquals("must be equal", 0, handlers.size());
        MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equal", 8, callList.size());
        assertEquals("must be equal", "startup : nil", callList.get(0));
        assertEquals("must be equal", "startup : nil", callList.get(1));
        assertEquals("must be equal", "startup : nil, submit msg", callList.get(2));
        assertEquals("must be equal", "handle msg : pass", callList.get(3));
        assertEquals("must be equal", "handle msg : null", callList.get(4));
        assertEquals("must be equal", "shutdown : nil", callList.get(5));
        assertEquals("must be equal", "shutdown : nil", callList.get(6));
        assertEquals("must be equal", "shutdown : nil", callList.get(7));
    }

}
