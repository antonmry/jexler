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

import net.jexler.service.ServiceState
import net.jexler.test.SlowTests

import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class JexlerContainerSlowSpec extends Specification {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    private final static long MS_1_SEC = 1000
    private final static long MS_3_SEC = 3000
    private final static long MS_6_SEC = 6000
    
    def 'TEST SLOW (8 sec) startup and shutdown too slower than time waited'() {
        given:
        def dir = tempFolder.root
        def jexlerBodyFast = """\
            while (true) {
              event = events.take()
              if (event instanceof StopEvent) {
                return
              }
            }
            """.stripIndent()
        def jexlerBodySlow = """\
            log.info('before startup wait ' + jexler.id)
            JexlerUtil.waitAtLeast($MS_3_SEC)
            log.info('after startup wait ' + jexler.id)
            while (true) {
              event = events.take()
              if (event instanceof StopEvent) {
                // set explicitly because cannot easily wait after returns
                jexler.state = ServiceState.BUSY_STOPPING
                JexlerUtil.waitAtLeast($MS_3_SEC)
                return
              }
            }
            """.stripIndent()
        new File(dir, 'Jexler1.groovy').text = "['autostart':true]\n$jexlerBodySlow"
        new File(dir, 'Jexler2.groovy').text = "['autostart':true]\n$jexlerBodyFast"
        new File(dir, 'Jexler3.groovy').text = "['autostart':true]\n$jexlerBodySlow"

        when:
        def container = new JexlerContainer(dir)
        def jexler1 = container.getJexler('Jexler1')
        def jexler2 = container.getJexler('Jexler2')
        def jexler3 = container.getJexler('Jexler3')

        then:
        container.jexlers.size() == 3
        container.state.off
        jexler1.state.off
        jexler2.state.off
        jexler3.state.off

        when:
        container.start()
        JexlerUtil.waitForStartup(container, MS_1_SEC)

        then:
        container.state == ServiceState.BUSY_STARTING
        jexler1.state == ServiceState.BUSY_STARTING
        jexler2.state == ServiceState.IDLE
        jexler3.state == ServiceState.BUSY_STARTING
        container.issues.size() == 2
        container.issues.each { issue ->
            assert issue.message == 'Timeout waiting for jexler startup.'
        }

        when:
        container.forgetIssues()
        JexlerUtil.waitForStartup(container, MS_6_SEC)

        then:
        container.state == ServiceState.IDLE
        jexler1.state == ServiceState.IDLE
        jexler2.state == ServiceState.IDLE
        jexler3.state == ServiceState.IDLE
        container.issues.empty

        when:
        container.stop()
        JexlerUtil.waitForShutdown(container, MS_1_SEC)

        then:
        container.state == ServiceState.BUSY_STOPPING
        jexler1.state == ServiceState.BUSY_STOPPING
        jexler2.state == ServiceState.OFF
        jexler3.state == ServiceState.BUSY_STOPPING
        container.issues.size() == 2
        container.issues.each { issue ->
            assert issue.message == 'Timeout waiting for jexler shutdown.'
        }

        when:
        container.forgetIssues()
        JexlerUtil.waitForShutdown(container, MS_6_SEC)

        then:
        container.state == ServiceState.OFF
        jexler1.state == ServiceState.OFF
        jexler2.state == ServiceState.OFF
        jexler3.state == ServiceState.OFF
        container.issues.empty
    }

}
