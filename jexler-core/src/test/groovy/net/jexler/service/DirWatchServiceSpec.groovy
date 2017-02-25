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

import net.jexler.TestJexler
import net.jexler.test.FastTests

import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.StandardWatchEventKinds

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class DirWatchServiceSpec extends Specification {

    def 'TEST no watch dir'() {
        given:
        def dir = new File('does-not-exist')
        def jexler = new TestJexler()

        when:
        def service = new DirWatchService(jexler, 'watchid')
                .setDir(dir)
                .setKinds([ StandardWatchEventKinds.ENTRY_CREATE ])
                .setModifiers([])
                .setCron('* * * * *')
                .setScheduler(null)
        service.start()

        then:
        service.state.off
        service.dir == dir
        service.kinds.size() == 1
        service.kinds.first() == StandardWatchEventKinds.ENTRY_CREATE
        service.modifiers.empty
        service.cron == '0 * * * * ?'
        service.scheduler == null
        jexler.issues.size() == 1
        jexler.issues.first().service == service
        jexler.issues.first().message.startsWith('Could not create watch service or key')
        jexler.issues.first().cause instanceof IOException
    }

}
