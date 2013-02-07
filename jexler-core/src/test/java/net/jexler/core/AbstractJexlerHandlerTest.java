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

import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class AbstractJexlerHandlerTest {

    private class SimpleJexlerHandler extends AbstractJexlerHandler {
        public SimpleJexlerHandler(String id, String description) {
            super(id, description);
        }
    }

    @Test
    public void testAll() {
        AbstractJexlerHandler handler = new SimpleJexlerHandler("myid", "my description");
        handler.startup(null);
        assertFalse("must be false", handler.canHandle(null));
        assertFalse("must be false", handler.handle(null));
        handler.shutdown();
        assertEquals("must be equals", "myid", handler.getId());
        assertEquals("must be equals", "my description", handler.getDescription());
     }

}