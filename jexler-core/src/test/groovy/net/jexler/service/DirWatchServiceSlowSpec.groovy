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

import net.jexler.Jexler
import net.jexler.TestJexler
import net.jexler.test.SlowTests

import com.sun.nio.file.SensitivityWatchEventModifier
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.StandardWatchEventKinds

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class DirWatchServiceSlowSpec extends Specification {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final static long MS_3_SEC = 3000
    private final static String CRON_EVERY_SEC = '*/1 * * * * *'

    def 'TEST SLOW (40 sec) create/modify/remove files in watch dir'() {
        given:
        def watchDir = tempFolder.root
        def jexler = new TestJexler();

        when:
        def service = new DirWatchService(jexler, 'watchid')
        service.dir = watchDir
        service.cron = CRON_EVERY_SEC
        service.modifiers = [ SensitivityWatchEventModifier.HIGH ]

        then:
        service.id == 'watchid'

        when:
        service.start()

        then:
        service.state.on
        ServiceUtil.waitForStartup(service, MS_3_SEC)
        jexler.takeEvent(MS_3_SEC) == null

        when:
        checkCreateModifyDeleteEventsTriggered(jexler, service, watchDir)

        service.stop()

        then:
        service.state.off
        ServiceUtil.waitForShutdown(service, MS_3_SEC)

        when:
        // create file after service stop
        new File(watchDir, 'temp2').text = 'hello too'

        then:
        jexler.takeEvent(MS_3_SEC) == null

        when:
        // different watch directory
        watchDir = tempFolder.newFolder()
        service.dir = watchDir
        service.start()

        then:
        service.state.on
        ServiceUtil.waitForStartup(service, MS_3_SEC)
        jexler.takeEvent(MS_3_SEC) == null

        when:
        service.start()

        then:
        service.state == ServiceState.IDLE

        when:
        checkCreateModifyDeleteEventsTriggered(jexler, service, watchDir)

        then:
        // delete watch directory
        watchDir.delete()
        jexler.takeEvent(MS_3_SEC) == null

        when:
        service.stop()

        then:
        ServiceUtil.waitForShutdown(service, MS_3_SEC)

        when:
        service.stop()

        then:
        service.state.off

        when:
        // different watch directory
        watchDir = tempFolder.newFolder()
        service.dir = watchDir
        service.start()

        then:
        service.state.on

        when:
        service.zap()

        then:
        service.state == ServiceState.OFF

        when:
        service.zap()

        then:
        service.state == ServiceState.OFF
    }

    private static void checkCreateModifyDeleteEventsTriggered(Jexler jexler, Service service, File watchDir) {

        // create file
        def tempFile = new File(watchDir, 'temp')
        tempFile.createNewFile()

        def event = jexler.takeEvent(MS_3_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_CREATE
        assert jexler.takeEvent(MS_3_SEC) == null

        // modify file
        tempFile.text = 'hello there'

        event = jexler.takeEvent(MS_3_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_MODIFY
        assert jexler.takeEvent(MS_3_SEC) == null

        // delete file
        assert tempFile.delete()

        event = jexler.takeEvent(MS_3_SEC)
        assert event instanceof DirWatchEvent
        assert event.service == service
        assert event.file.canonicalPath == tempFile.canonicalPath
        assert event.kind == StandardWatchEventKinds.ENTRY_DELETE
        assert jexler.takeEvent(MS_3_SEC) == null
    }

}
