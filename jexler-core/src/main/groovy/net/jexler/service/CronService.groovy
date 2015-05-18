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

import groovy.transform.CompileStatic
import it.sauronsoftware.cron4j.Scheduler
import net.jexler.Jexler
import net.jexler.RunState

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A cron service, creates events at configurable times.
 * Implemented using the cron4j library.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class CronService extends ServiceBase {

    private static final Logger log = LoggerFactory.getLogger(CronService.class)
    
    public static final String CRON_NOW = 'now'
    public static final String CRON_NOW_AND_STOP = "$CRON_NOW+stop"

    private Scheduler scheduler
    private String cron
    private String scheduledId

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    CronService(Jexler jexler, String id) {
        super(jexler, id)
    }

    /**
     * Set cron pattern, e.g. "* * * * *".
     * Use "now" for now, i.e. for a single event immediately,
     * or "now+stop" for a single event immediately, followed
     * by a StopEvent, which can both be useful for testing.
     * @return this (for chaining calls)
     */
    CronService setCron(String cron) {
        this.cron = cron
        return this
    }

    /**
     * Set cron4j scheduler.
     * Default is a scheduler shared by all jexlers in the same jexler container.
     * @return this (for chaining calls)
     */
    CronService setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler
        return this
    }

    @Override
    void start() {
        if (!off) {
            return
        }
        if (cron.startsWith(CRON_NOW)) {
            log.trace("new cron event: $cron")
            jexler.handle(new CronEvent(this, cron))
            runState = RunState.IDLE
            if (cron.equals(CRON_NOW_AND_STOP)) {
                jexler.handle(new StopEvent(this))
                runState = RunState.OFF
            }
            return
        }
        final CronService thisService = this
        Thread cronThread = new Thread() {
            void run() {
                currentThread().name = "$jexler.id|$thisService.id"
                log.trace("new cron event: $cron")
                jexler.handle(new CronEvent(thisService, cron))
            }
        }
        cronThread.daemon = true
        runState = RunState.IDLE
        if (scheduler == null) {
            scheduler = jexler.container.sharedScheduler
        }
        scheduledId = scheduler.schedule(cron, cronThread)
    }

    @Override
    void stop() {
        if (off) {
            return
        }
        if (scheduler != null) {
            scheduler.deschedule(scheduledId)
        }
        runState = RunState.OFF
    }

}
