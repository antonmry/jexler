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

import it.sauronsoftware.cron4j.Scheduler
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
class QuartzServiceSpec extends Specification {

    def 'TEST basic construct and set'() {
        given:
        def jexler = new TestJexler()
        def service = new QuartzService(jexler, 'quartzid')
        service.setQuartz('0 * * * * *')
    }

}
