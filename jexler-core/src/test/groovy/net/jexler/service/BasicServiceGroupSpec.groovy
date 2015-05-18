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

import net.jexler.RunState
import net.jexler.TestJexler
import net.jexler.test.FastTests
import org.junit.experimental.categories.Category
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class BasicServiceGroupSpec extends Specification {

    def "basics including group run state"() {
        given:
        def jexler = new TestJexler()
        def service1 = new MockService(jexler, 'service1')
        def service2 = new MockService(jexler, 'service2')
        def service3 = new MockService(jexler, 'service3')

        when:
        def group = new ServiceGroup('group')

        then:
        group.id == 'group'
        group.services.empty

        when:
        group.add(service1)
        group.add(service2)
        group.add(service3)

        then:
        group.services.size() == 3
        group.runState == RunState.OFF

        when:
        service2.runState = RunState.BUSY_STARTING

        then:
        group.runState == RunState.BUSY_STARTING

        when:
        service1.runState = RunState.IDLE
        service3.runState = RunState.BUSY_EVENT

        then:
        group.runState == RunState.BUSY_STARTING

        when:
        service2.runState = RunState.IDLE

        then:
        group.runState == RunState.IDLE

        when:
        service1.runState = RunState.BUSY_STOPPING
        service2.runState = RunState.BUSY_STOPPING
        service3.runState = RunState.BUSY_STOPPING

        then:
        group.runState == RunState.BUSY_STOPPING
    }

    def "start and stop"() {
        given:
        def jexler = new TestJexler()
        def service1 = new MockService(jexler, 'service1')
        def service2 = new MockService(jexler, 'service2')
        def service3 = new MockService(jexler, 'service3')

        when:
        def group = new ServiceGroup('group')
        group.add(service1)
        group.add(service2)
        group.add(service3)

        then:
        service1.runState == RunState.OFF
        service2.runState == RunState.OFF
        service3.runState == RunState.OFF
        group.runState == RunState.OFF
        !group.on
        group.off

        when:
        service1.start()

        then:
        service1.runState == RunState.IDLE
        service2.runState == RunState.OFF
        service3.runState == RunState.OFF
        group.runState == RunState.IDLE
        group.on
        !group.off

        when:
        group.start()

        then:
        service1.runState == RunState.IDLE
        service2.runState == RunState.IDLE
        service3.runState == RunState.IDLE
        group.runState == RunState.IDLE
        group.on
        !group.off

        when:
        service3.stop()

        then:
        service1.runState == RunState.IDLE
        service2.runState == RunState.IDLE
        service3.runState == RunState.OFF
        group.runState == RunState.IDLE
        group.on
        !group.off

        when:
        group.stop()

        then:
        service1.runState == RunState.OFF
        service2.runState == RunState.OFF
        service3.runState == RunState.OFF
        group.runState == RunState.OFF
        !group.on
        group.off
    }

    def "runtime exceptions when stopping services"() {
        given:
        def jexler = new TestJexler()
        def service1 = new MockService(jexler, 'service1')
        def service2 = new MockService(jexler, 'service2')
        def service3 = new MockService(jexler, 'service3')
        def group = new ServiceGroup('group')
        group.add(service1)
        group.add(service2)
        group.add(service3)

        when:
        group.start()

        then:
        service1.runState == RunState.IDLE
        service2.runState == RunState.IDLE
        service3.runState == RunState.IDLE
        group.runState == RunState.IDLE

        when:
        RuntimeException ex1 = new RuntimeException()
        RuntimeException ex2 = new RuntimeException()
        service1.setStopRuntimeException(ex1)
        service2.setStopRuntimeException(ex2)
        group.stop()

        then:
        RuntimeException e = thrown()
        e.is(ex1)
        service1.runState == RunState.IDLE
        service2.runState == RunState.IDLE
        service3.runState == RunState.OFF
        group.runState == RunState.IDLE
    }

    def "empty service group"() {
        when:
        def group = new ServiceGroup("group")

        then:
        group.runState == RunState.OFF

        when:
        group.start()

        then:
        group.runState == RunState.OFF

        when:
        group.stop()

        then:
        group.runState == RunState.OFF
    }

}
