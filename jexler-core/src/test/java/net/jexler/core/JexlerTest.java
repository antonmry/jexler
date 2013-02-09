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

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class JexlerTest {

    private Jexler mockJexler;
    private List<JexlerHandler> handlers;

    public void clearHandlerCalls() {
        for (JexlerHandler handler : handlers) {
            ((MockHandler)handler).calls.clear();
        }
    }

    @Before
    public void setup() {
        mockJexler = JexlerFactory.getJexler(new File("src/test/mocksuite/mock1"));
        assertFalse("must be false", mockJexler.isRunning());

        mockJexler.start();
        assertTrue("must be true", mockJexler.isRunning());
        handlers = mockJexler.getHandlers();
        assertEquals("must be equal", 5, handlers.size());
        for (JexlerHandler handler : handlers) {
            List<String> calls = ((MockHandler)handler).calls;
            assertEquals("must be equal", 1, calls.size());
            assertEquals("must be equal", "startup : nil", calls.get(0));
        }

        clearHandlerCalls();
    }


    @Test
    public void testStop() {
        mockJexler.stop();
        assertFalse("must be false", mockJexler.isRunning());
        assertEquals("must be equal", 0, mockJexler.getHandlers().size());
        for (JexlerHandler handler : handlers) {
            List<String> calls = ((MockHandler)handler).calls;
            assertEquals("must be equal", 1, calls.size());
            assertEquals("must be equal", "shutdown : nil", calls.get(0));
        }
    }

}
