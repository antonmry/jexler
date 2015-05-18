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
import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.Files

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class JexlerSlowSpec extends Specification {
    
    private final static long MS_5_SEC = 5000
    private final static long MS_20_SEC = 20000
    
    def "jexer start or shutdown too slow"() {
        given:
        File dir = Files.createTempDirectory(null).toFile()
        File file = new File(dir, 'Test.groovy')
        file.text = """\
            log.info('before startup wait ' + jexler.id)
            JexlerUtil.waitAtLeast(10000)
            log.info('after startup wait ' + jexler.id)
            while (true) {
              event = events.take()
              if (event instanceof StopEvent) {
                JexlerUtil.waitAtLeast(10000)
                return
              }
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(MS_5_SEC)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Timeout waiting for jexler startup.'

        when:
        jexler.forgetIssues()
        jexler.waitForStartup(MS_20_SEC)

        then:
        jexler.issues.empty

        when:
        jexler.stop()
        jexler.waitForShutdown(MS_5_SEC)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Timeout waiting for jexler shutdown.'

        when:
        jexler.forgetIssues()
        jexler.waitForShutdown(MS_20_SEC)

        then:
        jexler.issues.empty
    }

}