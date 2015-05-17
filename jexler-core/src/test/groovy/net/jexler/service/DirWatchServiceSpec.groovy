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

import spock.lang.Specification

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import it.sauronsoftware.cron4j.Scheduler
import net.jexler.Issue
import net.jexler.MockJexler
import net.jexler.test.FastTests

import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class DirWatchServiceSpec extends Specification {

    def "no watch dir"() {
        given:
        def watchDir = new File("does-not-exist")
        def jexler = new MockJexler()

        when:
        def service = new DirWatchService(jexler, "watchid")
        service.dir = watchDir
        service.cron = "* * * * *"
        service.scheduler = new Scheduler()
        service.start()

        then:
        service.off
        jexler.issues.size() == 1
        jexler.issues.first().service == service
        jexler.issues.first().message.startsWith('Could not create watch service or key')
        jexler.issues.first().cause instanceof IOException
    }

}
