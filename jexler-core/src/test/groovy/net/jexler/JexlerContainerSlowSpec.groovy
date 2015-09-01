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
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final static long MS_5_SEC = 5000
    private final static long MS_20_SEC = 20000
    
    def 'TEST SLOW (30 sec) startup and shutdown too slower than time waited'() {
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
            JexlerUtil.waitAtLeast(10000)
            log.info('after startup wait ' + jexler.id)
            while (true) {
              event = events.take()
              if (event instanceof StopEvent) {
                // set explicitly because cannot easily wait after returns
                jexler.runState = RunState.BUSY_STOPPING
                JexlerUtil.waitAtLeast(10000)
                return
              }
            }
            """.stripIndent()
        new File(dir, 'Jexler1.groovy').text = "//\n$jexlerBodySlow"
        new File(dir, 'Jexler2.groovy').text = "//\n$jexlerBodyFast"
        new File(dir, 'Jexler3.groovy').text = "//\n$jexlerBodySlow"

        when:
        def container = new JexlerContainer(dir)
        def jexler1 = container.getJexler('Jexler1')
        def jexler2 = container.getJexler('Jexler2')
        def jexler3 = container.getJexler('Jexler3')

        then:
        container.jexlers.size() == 3
        container.off
        jexler1.off
        jexler2.off
        jexler3.off

        when:
        container.start()
        container.waitForStartup(MS_5_SEC)

        then:
        container.runState == RunState.BUSY_STARTING
        jexler1.runState == RunState.BUSY_STARTING
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.BUSY_STARTING
        container.issues.size() == 2
        container.issues.each { issue ->
            assert issue.message == 'Timeout waiting for jexler startup.'
        }

        when:
        container.forgetIssues()
        container.waitForStartup(MS_20_SEC)

        then:
        container.runState == RunState.IDLE
        jexler1.runState == RunState.IDLE
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.IDLE
        container.issues.empty

        when:
        container.stop()
        container.waitForShutdown(MS_5_SEC)

        then:
        container.runState == RunState.IDLE
        jexler1.runState == RunState.BUSY_STOPPING
        jexler2.runState == RunState.OFF
        jexler3.runState == RunState.BUSY_STOPPING
        container.issues.size() == 2
        container.issues.each { issue ->
            assert issue.message == 'Timeout waiting for jexler shutdown.'
        }

        when:
        container.forgetIssues()
        container.waitForShutdown(MS_20_SEC)

        then:
        container.runState == RunState.OFF
        jexler1.runState == RunState.OFF
        jexler2.runState == RunState.OFF
        jexler3.runState == RunState.OFF
        container.issues.empty
    }

}
