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

import net.jexler.service.ServiceState
import net.jexler.test.FastTests

import org.junit.experimental.categories.Category
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class ServiceStateSpec extends Specification {

    def 'TEST elementary'() {
        expect:
        ServiceState.OFF.info == 'off'
        ServiceState.valueOf('OFF') == ServiceState.OFF
    }

    def 'TEST state matrix'() {
        expect:
        state.off == off
        state.on == on
        state.operational == operational
        state.busyStarting == busyStarting
        state.idle == idle
        state.busyEvent == busyEvent
        state.busyStopping == busyStopping
        state.busy == busy

        where:
        state                      | off   | on    | operational | busyStarting | idle  | busyEvent | busyStopping | busy
        ServiceState.OFF           | true  | false | false       | false        | false | false     | false        | false
        ServiceState.BUSY_STARTING | false | true  | false       | true         | false | false     | false        | true
        ServiceState.IDLE          | false | true  | true        | false        | true  | false     | false        | false
        ServiceState.BUSY_EVENT    | false | true  | true        | false        | false | true      | false        | true
        ServiceState.BUSY_STOPPING | false | true  | false       | false        | false | false     | true         | true
    }

}
