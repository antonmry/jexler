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

import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class JexlerSuiteTest {

    @Test
    public void testSuite() {
        JexlerSuite suite = JexlerSuiteFactory.getSuite(new File("src/test/testjexlers"));

        List<Jexler> jexlers = suite.getJexlers();
        assertEquals("must be equals", 4, jexlers.size());
        int i = 1;
        for (Jexler jexler : jexlers) {
            assertFalse("must be false", jexler.isRunning());
            // must be sorted alphabetically
            String expectedId = "mock" + i;
            assertEquals("must be equals", expectedId, jexler.getId());
            assertEquals("must be equals", jexler, suite.getJexler(expectedId));
            i++;
        }

        suite.start();
        assertTrue("must be true", jexlers.get(0).isRunning());
        assertFalse("must be false", jexlers.get(1).isRunning());
        assertTrue("must be true", jexlers.get(2).isRunning());
        assertTrue("must be true", jexlers.get(3).isRunning());

        jexlers.get(0).stop();
        assertFalse("must be false", jexlers.get(0).isRunning());
        assertFalse("must be false", jexlers.get(1).isRunning());
        assertTrue("must be true", jexlers.get(2).isRunning());
        assertTrue("must be true", jexlers.get(3).isRunning());

        suite.stop();
        assertFalse("must be false", jexlers.get(0).isRunning());
        assertFalse("must be false", jexlers.get(1).isRunning());
        assertFalse("must be false", jexlers.get(2).isRunning());
        assertFalse("must be false", jexlers.get(3).isRunning());

        jexlers.get(0).start();
        assertTrue("must be true", jexlers.get(0).isRunning());
        assertFalse("must be false", jexlers.get(1).isRunning());
        assertFalse("must be false", jexlers.get(2).isRunning());
        assertFalse("must be false", jexlers.get(3).isRunning());

        suite.start();
        assertTrue("must be true", jexlers.get(0).isRunning());
        assertFalse("must be false", jexlers.get(1).isRunning());
        assertTrue("must be true", jexlers.get(2).isRunning());
        assertTrue("must be true", jexlers.get(3).isRunning());

        suite.stop();
    }

}
