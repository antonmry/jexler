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

    private final static long MS_1_MIN_10_SEC = 70000
    private final static long MS_30_SEC = 30000
    private final static long MS_10_SEC = 10000
    private final static String CRON_EVERY_MIN = '* * * * *'

    def "cron every minute (~4 min)"() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid')
        service.cron = CRON_EVERY_MIN

        then:
        service.id == 'cronid'
        service.cron == CRON_EVERY_MIN

        when:
        service.start()

        then:
        service.on
        service.waitForStartup(MS_30_SEC)

        when:
        def event = jexler.takeEvent(MS_1_MIN_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CRON_EVERY_MIN

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_30_SEC)
        service.off
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        service.start()

        then:
        service.on
        service.waitForStartup(MS_30_SEC)

        when:
        service.start()

        then:
        service.runState == RunState.IDLE

        when:
        event = jexler.takeEvent(MS_1_MIN_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CRON_EVERY_MIN

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_30_SEC)
        service.off
        jexler.takeEvent(MS_1_MIN_10_SEC) == null

        when:
        service.stop()

        then:
        service.off

        cleanup:
        jexler.container.close()
    }

    def "cron now (~1 min)"() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid').setCron(CronService.CRON_NOW)
        def event = jexler.takeEvent(MS_10_SEC)

        then:
        event == null

        when:
        service.start()

        then:
        service.on

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW
        jexler.takeEvent(MS_10_SEC) == null

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_10_SEC)
        service.off
        jexler.takeEvent(MS_10_SEC) == null

        when:
        service.start()

        then:
        service.on

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW
        jexler.takeEvent(MS_10_SEC) == null

        when:
        service.stop()

        then:
        service.waitForShutdown(MS_10_SEC)
        service.off
        jexler.takeEvent(MS_10_SEC) == null
    }

    def "cron now+stop (~1 min)"() {
        given:
        def jexler = new TestJexler()

        when:
        def service = new CronService(jexler, 'cronid').setCron(CronService.CRON_NOW_AND_STOP)
        def event = jexler.takeEvent(MS_10_SEC)

        then:
        event == null

        when:
        service.start()

        then:
        service.off

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW_AND_STOP

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event instanceof StopEvent
        jexler.takeEvent(MS_10_SEC) == null

        when:
        service.start()

        then:
        service.off

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event.service.is(service)
        event instanceof CronEvent
        event.cron == CronService.CRON_NOW_AND_STOP

        when:
        event = jexler.takeEvent(MS_10_SEC)

        then:
        event instanceof StopEvent
        jexler.takeEvent(MS_10_SEC) == null
    }

}
