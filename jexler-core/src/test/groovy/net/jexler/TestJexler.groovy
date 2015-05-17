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

package net.jexler

import groovy.transform.CompileStatic

import java.nio.file.Files

import net.jexler.service.Event

/**
 * Jexler implementation for unit tests, allows to poll events from event queue.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class TestJexler extends Jexler {

    TestJexler(File file, JexlerContainer container) {
        super(file, container)
    }

    TestJexler(File file) throws Exception {
        this(file, new JexlerContainer(file.parentFile))
    }

    TestJexler() throws Exception {
        this(new File(Files.createTempDirectory(null).toFile(), "TestJexler.groovy"))
    }

    /**
     * Wait at most timeout ms for event, return
     * event if got one in time, null otherwise.
     */
    Event takeEvent(long timeout) {
        long t0 = System.currentTimeMillis()
        while (true) {
            Event event = events.poll()
            if (event != null) {
                return event
            }
            if (System.currentTimeMillis() - t0 > timeout) {
                return null
            }
            try {
                Thread.sleep(10)
            } catch (InterruptedException e) {
            }
        }
    }

}
