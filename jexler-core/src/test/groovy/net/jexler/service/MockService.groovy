/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.jexler.service

import net.jexler.Jexler

import groovy.transform.CompileStatic

/**
 * Mock service implementation for unit tests.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class MockService extends ServiceBase {

    private static final Map<String,MockService> instances = new HashMap<>()

    private Jexler jexler
    volatile int nStarted = 0
    volatile int nStopped = 0
    volatile int nZapped = 0
    volatile int nEventsSent = 0
    volatile int nEventsGotBack = 0
    volatile RuntimeException stopRuntimeException = null

    static MockService getInstance(String id) {
        synchronized(instances) {
            return instances.get(id)
        }
    }

    MockService(Jexler jexler, String id) {
        super(id)
        this.jexler = jexler
        synchronized(instances) {
            instances.put(id, this)
        }
    }

    @Override
    void start() {
        nStarted++
        state = ServiceState.IDLE
    }

    @Override
    void stop() {
        nStopped++
        if (stopRuntimeException != null) {
            throw stopRuntimeException
        }
        state = ServiceState.OFF
    }

    @Override
    void zap() {
        nZapped++
        nStopped++
        state = ServiceState.OFF
    }

    void notifyGotEvent() {
        nEventsGotBack++
    }

    void notifyJexler() {
        jexler.handle(new MockEvent(this))
        nEventsSent++
    }

    void notifyJexler(Event event) {
        jexler.handle(event)
        nEventsSent++
    }

}
