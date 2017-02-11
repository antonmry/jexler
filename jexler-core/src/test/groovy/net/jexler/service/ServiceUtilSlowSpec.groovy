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

package net.jexler.service

import net.jexler.JexlerUtil
import net.jexler.test.SlowTests

import org.junit.experimental.categories.Category
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class ServiceUtilSlowSpec extends Specification {

    private final static long MS_3_SEC = 3000
    private final static long MS_6_SEC = 6000
    private final static long MS_9_SEC = 9000

    static class InterruptingThread extends Thread {
        Thread threadToInterrupt
        long interruptAfterMs
        boolean hasInterrupted
        InterruptingThread(Thread threadToInterrupt, long interruptAfterMs) {
            this.threadToInterrupt = threadToInterrupt
            this.interruptAfterMs = interruptAfterMs
        }
        @Override
        void run() {
            JexlerUtil.waitAtLeast(interruptAfterMs)
            threadToInterrupt.interrupt()
            hasInterrupted = true
        }
    }

    static class ServiceStateSettingThread extends Thread {
        Service service
        ServiceState stateToSet
        long setAfterMs
        ServiceStateSettingThread(Service service, ServiceState stateToSet, long setAfterMs) {
            this.service = service
            this.stateToSet = stateToSet
            this.setAfterMs = setAfterMs
        }
        @Override
        void run() {
            JexlerUtil.waitAtLeast(setAfterMs)
            service.state = stateToSet
        }
    }

    def 'TEST SLOW (6 sec) wait for startup'() {
        given:
        def service = new MockService(null, 'mock')

        when:
        service.state = ServiceState.BUSY_STARTING

        then:
        !ServiceUtil.waitForStartup(service, 0)

        when:
        def interruptingThread = new InterruptingThread(Thread.currentThread(), MS_3_SEC)
        interruptingThread.start()
        new ServiceStateSettingThread(service, ServiceState.IDLE, MS_6_SEC).start()

        then:
        ServiceUtil.waitForStartup(service, MS_9_SEC)
        interruptingThread.hasInterrupted
        service.state == ServiceState.IDLE
    }

    def 'TEST SLOW (6 sec) wait for shutdown'() {
        given:
        def service = new MockService(null, 'mock')

        when:
        service.state = ServiceState.IDLE

        then:
        !ServiceUtil.waitForShutdown(service, 0)

        when:
        def interruptingThread = new InterruptingThread(Thread.currentThread(), MS_3_SEC)
        interruptingThread.start()
        new ServiceStateSettingThread(service, ServiceState.OFF, MS_6_SEC).start()

        then:
        ServiceUtil.waitForShutdown(service, MS_9_SEC)
        interruptingThread.hasInterrupted
        service.state == ServiceState.OFF
    }

}
