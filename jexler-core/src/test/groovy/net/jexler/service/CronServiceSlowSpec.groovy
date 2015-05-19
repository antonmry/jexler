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

import net.jexler.RunState
import net.jexler.TestJexler
import net.jexler.test.SlowTests
import org.junit.experimental.categories.Category
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
class CronServiceSlowSpec extends Specification {

    private final static long MS_15_SEC = 15000
    private final static long MS_1_SEC = 1000
    private final static String QUARTZ_CRON_EVERY_10_SECS = '*/10 * * * * ?'

    def 'TEST SLOW (1.5 min) cron every minute'() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid')
        service.cron = QUARTZ_CRON_EVERY_10_SECS

        then:
        service.id == 'cronid'
        service.cron == QUARTZ_CRON_EVERY_10_SECS

        when:
        service.start()

        then:
        service.on
        service.waitForStartup(MS_15_SEC)

        when:
        def event = jexler.takeEvent(MS_15_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == QUARTZ_CRON_EVERY_10_SECS

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_15_SEC)
        service.off
        jexler.takeEvent(MS_15_SEC) == null

        when:
        service.start()

        then:
        service.on
        service.waitForStartup(MS_15_SEC)

        when:
        service.start()

        then:
        service.runState == RunState.IDLE

        when:
        event = jexler.takeEvent(MS_15_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == QUARTZ_CRON_EVERY_10_SECS

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_15_SEC)
        service.off
        jexler.takeEvent(MS_15_SEC) == null

        when:
        service.stop()

        then:
        service.off

        cleanup:
        jexler.container.close()
    }

    def 'TEST SLOW (10 sec) cron now'() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid').setCron(CronService.CRON_NOW)
        def event = jexler.takeEvent(MS_1_SEC)

        then:
        event == null

        when:
        service.start()

        then:
        service.on

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW
        jexler.takeEvent(MS_1_SEC) == null

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_15_SEC)
        service.off
        jexler.takeEvent(MS_1_SEC) == null

        when:
        service.start()

        then:
        service.on

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW
        jexler.takeEvent(MS_1_SEC) == null

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_15_SEC)
        service.off
        jexler.takeEvent(MS_1_SEC) == null
    }

    def 'TEST SLOW (10 sec) cron now+stop'() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid').setCron(CronService.CRON_NOW_AND_STOP)
        def event = jexler.takeEvent(MS_1_SEC)

        then:
        event == null

        when:
        service.start()

        then:
        service.off

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW_AND_STOP

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event instanceof StopEvent
        jexler.takeEvent(MS_1_SEC) == null

        when:
        service.start()

        then:
        service.off

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW_AND_STOP

        when:
        event = jexler.takeEvent(MS_1_SEC)

        then:
        event instanceof StopEvent
        jexler.takeEvent(MS_1_SEC) == null
    }

}
