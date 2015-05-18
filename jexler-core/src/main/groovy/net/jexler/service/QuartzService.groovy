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
import net.jexler.Jexler
import net.jexler.RunState
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A quartz service, creates events at configurable times.
 * Implemented using the quartz library.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class QuartzService extends ServiceBase {

    private static final Logger log = LoggerFactory.getLogger(QuartzService.class)

    public static final String QUARTZ_NOW = 'now'
    public static final String QUARTZ_NOW_AND_STOP = "$QUARTZ_NOW+stop"

    private final QuartzService thisService
    private Scheduler scheduler
    private String quartz
    private TriggerKey triggerKey

    static String xyz

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    QuartzService(Jexler jexler, String id) {
        super(jexler, id)
        thisService = this
    }

    /**
     * Set quartz pattern, e.g. "0 * * * * *".
     * Use "now" for now, i.e. for a single event immediately,
     * or "now+stop" for a single event immediately, followed
     * by a StopEvent, which can both be useful for testing.
     * @return this (for chaining calls)
     */
    QuartzService setQuartz(String quartz) {
        this.quartz = quartz
        return this
    }

    /**
     * Set quartz scheduler.
     * Default is a scheduler shared by all jexlers in the same jexler container.
     * @return this (for chaining calls)
     */
    QuartzService setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler
        return this
    }

    @Override
    void start() {
        if (!off) {
            return
        }
        if (quartz.startsWith(QUARTZ_NOW)) {
            log.trace("new quartz event: $quartz")
            jexler.handle(new QuartzEvent(this, quartz))
            runState = RunState.IDLE
            if (quartz.equals(QUARTZ_NOW_AND_STOP)) {
                jexler.handle(new StopEvent(this))
                runState = RunState.OFF
            }
            return
        }
        runState = RunState.IDLE
        if (scheduler == null) {
            scheduler = StdSchedulerFactory.getDefaultScheduler()
            scheduler.start()
        }

        // some groovy magic
        Class clazz = new GroovyClassLoader().parseClass("""\
            import groovy.transform.CompileStatic
            import org.quartz.*
            import net.jexler.Jexler
            import net.jexler.service.QuartzService
            import net.jexler.service.QuartzEvent
            import org.slf4j.Logger
            @CompileStatic
            class QuartzJob implements Job {
              public static Logger log
              public static Jexler jexler
              public static QuartzService quartzService
              public static String quartz
              QuartzJob() {
                log.trace("constructed")
              }
              void execute(JobExecutionContext context) throws JobExecutionException {
                Thread.currentThread().name = "\$jexler.id|\$quartzService.id"
                log.trace("new quartz event: \$quartz")
                jexler.handle(new QuartzEvent(quartzService, quartz))
              }
            }
            """)
        this.class.getDeclaredField('xyz').set(null, 'hello')
        log.trace(xyz)
        clazz.getDeclaredField('log').set(null, log)
        clazz.getDeclaredField('jexler').set(null, jexler)
        clazz.getDeclaredField('quartzService').set(null, this)
        clazz.getDeclaredField('quartz').set(null, quartz)

        JobDetail job = JobBuilder.newJob(clazz)
                .withIdentity("job-$id", "group-$id")
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-$id", "group-$id")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(quartz))
                .build();
        triggerKey = trigger.key
        scheduler.scheduleJob(job, trigger)
    }

    @Override
    void stop() {
        if (off) {
            return
        }
        if (scheduler != null) {
            scheduler.unscheduleJob(triggerKey)
        }
        runState = RunState.OFF
    }

}
