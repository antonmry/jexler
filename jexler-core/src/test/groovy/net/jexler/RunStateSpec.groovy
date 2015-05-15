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

import net.jexler.test.FastTests;
import org.junit.experimental.categories.Category;
import spock.lang.Specification;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class RunStateSpec extends Specification {

    def "general"() {
        expect:
        "off" == RunState.OFF.getInfo()
        RunState.OFF == RunState.valueOf("OFF")
    }

    def "off"() {
        expect:
        RunState.OFF.isOff()
        !RunState.OFF.isOn()
        !RunState.OFF.isOperational()
        !RunState.OFF.isBusyStarting()
        !RunState.OFF.isIdle()
        !RunState.OFF.isBusyEvent()
        !RunState.OFF.isBusyStopping()
    }

    def "busy (starting)"() {
        expect:
        !RunState.BUSY_STARTING.isOff()
        RunState.BUSY_STARTING.isOn()
        !RunState.BUSY_STARTING.isOperational()
        RunState.BUSY_STARTING.isBusyStarting()
        !RunState.BUSY_STARTING.isIdle()
        !RunState.BUSY_STARTING.isBusyEvent()
        !RunState.BUSY_STARTING.isBusyStopping()
    }

    def "idle"() {
        expect:
        !RunState.IDLE.isOff()
        RunState.IDLE.isOn()
        RunState.IDLE.isOperational()
        !RunState.IDLE.isBusyStarting()
        RunState.IDLE.isIdle()
        !RunState.IDLE.isBusyEvent()
        !RunState.IDLE.isBusyStopping()
    }

    def "busy (event)"() {
        expect:
        !RunState.BUSY_EVENT.isOff()
        RunState.BUSY_EVENT.isOn()
        RunState.BUSY_EVENT.isOperational()
        !RunState.BUSY_EVENT.isBusyStarting()
        !RunState.BUSY_EVENT.isIdle()
        RunState.BUSY_EVENT.isBusyEvent()
        !RunState.BUSY_EVENT.isBusyStopping()
    }

    def "busy (stopping)"() {
        expect:
        !RunState.BUSY_STOPPING.isOff()
        RunState.BUSY_STOPPING.isOn()
        !RunState.BUSY_STOPPING.isOperational()
        !RunState.BUSY_STOPPING.isBusyStarting()
        !RunState.BUSY_STOPPING.isIdle()
        !RunState.BUSY_STOPPING.isBusyEvent()
        RunState.BUSY_STOPPING.isBusyStopping()
    }

}
