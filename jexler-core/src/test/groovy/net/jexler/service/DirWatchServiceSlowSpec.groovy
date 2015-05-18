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

import net.jexler.Jexler
import net.jexler.RunState
import net.jexler.TestJexler
import net.jexler.test.SlowTests
import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.StandardWatchEventKinds

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class DirWatchServiceSlowSpec extends Specification {

    private final static long MS_1_MIN_10_SEC = 70000
    private final static long MS_30_SEC = 30000

    def 'TEST SLOW (12 min) create/modify/remove files in watch dir'() {
        given:
        def watchDir = Files.createTempDirectory(null).toFile()
        def jexler = new TestJexler();

        when:
        def service = new DirWatchService(jexler, 'watchid')
        service.dir = watchDir
        service.cron = '* * * * *'

        then:
        service.id == 'watchid'

        when:
        service.start()

        then:
        service.on
        service.waitForStartup(MS_30_SEC)
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        checkCreateModifyDeleteEventsTriggered(jexler, service, watchDir)

        service.stop()

        then:
        service.off
        service.waitForShutdown(MS_30_SEC)

        when:
        // create file after service stop
        new File(watchDir, 'temp2').text = 'hello too'

        then:
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        // different watch directory
        watchDir = Files.createTempDirectory(null).toFile()
        service.dir = watchDir
        service.start()

        then:
        service.on
        service.waitForStartup(MS_30_SEC)
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        service.start()

        then:
        service.runState == RunState.IDLE

        when:
        checkCreateModifyDeleteEventsTriggered(jexler, service, watchDir)

        then:
        // delete watch directory
        watchDir.delete()
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_30_SEC)

        when:
        service.stop()

        then:
        service.off
    }

    private void checkCreateModifyDeleteEventsTriggered(Jexler jexler, Service service, File watchDir) {

        // create file
        def tempFile = new File(watchDir, 'temp')
        Files.createFile(tempFile.toPath())

        def event = jexler.takeEvent(MS_1_MIN_10_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_CREATE
        assert jexler.takeEvent(MS_1_MIN_10_SEC) == null

        // modify file
        tempFile.text = 'hello there'

        event = jexler.takeEvent(MS_1_MIN_10_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_MODIFY
        assert jexler.takeEvent(MS_1_MIN_10_SEC) == null

        // delete file
        assert tempFile.delete()

        event = jexler.takeEvent(MS_1_MIN_10_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_DELETE
        assert jexler.takeEvent(MS_1_MIN_10_SEC) == null
    }

}
