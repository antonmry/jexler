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

import groovy.transform.CompileStatic
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * Directory watch service, creates an event when a file
 * in a given directory is created, modified oder deleted.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class DirWatchService extends ServiceBase {

    private static final Logger log = LoggerFactory.getLogger(DirWatchService.class)

    /** Jexler. */
    final Jexler jexler

    /** Directory to watch. */
    File watchDir

    /** Cron pattern. */
    String cron

    /** Quartz scheduler. */
    Scheduler scheduler

    private TriggerKey triggerKey
    private WatchService watchService
    private WatchKey watchKey

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    DirWatchService(Jexler jexler, String id) {
        super(id)
        this.jexler = jexler
        watchDir = jexler.dir
        this.cron = '*/5 * * * * ?'
    }

    /**
     * Set directory to watch.
     * Default if not set is the directory that contains the jexler.
     * @param watchDir directory to watch
     * @return this (for chaining calls)
     */
    DirWatchService setDir(File watchDir) {
        this.watchDir = watchDir
        return this
    }

    /**
     * Set cron pattern for when to check.
     * Default is every 5 seconds.
     * @return this (for chaining calls)
     */
    DirWatchService setCron(String cron) {
        this.cron = ServiceUtil.toQuartzCron(cron)
        return this
    }

    /**
     * Set quartz scheduler.
     * Default is a scheduler shared by all jexlers in the same jexler container.
     * @return this (for chaining calls)
     */
    DirWatchService setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler
        return this
    }


    @Override
    void start() {
        if (state.on) {
            return
        }
        Path path = watchDir.toPath()
        try {
            watchService = path.fileSystem.newWatchService()
            watchKey = path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE)
        } catch (IOException e) {
            jexler.trackIssue(this,
                    "Could not create watch service or key for directory '$watchDir.absolutePath'.", e)
            return
        }

        String uuid = UUID.randomUUID()
        JobDetail job = JobBuilder.newJob(DirWatchJob.class)
                .withIdentity("job-$id-$uuid", jexler.id)
                .usingJobData(['service':this] as JobDataMap)
                .build()
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-$id-$uuid", jexler.id)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .startNow()
                .build()
        triggerKey = trigger.key

        if (scheduler == null) {
            scheduler = jexler.container.scheduler
        }
        scheduler.scheduleJob(job, trigger)
        state = ServiceState.IDLE
    }

    @Override
    void stop() {
        if (state.off) {
            return
        }
        scheduler.unscheduleJob(triggerKey)
        watchKey.cancel()
        try {
            watchService.close()
        } catch (IOException e) {
            log.trace('failed to close watch service', e)
        }
        state = ServiceState.OFF
    }

    @Override
    void zap() {
        if (state.off) {
            return
        }
        state = ServiceState.OFF
        new Thread() {
            void run() {
                try {
                    scheduler.unscheduleJob(triggerKey)
                } catch (Throwable t) {
                    log.trace('failed to unschedule cron job', t)
                }
                try {
                    watchKey.cancel()
                    watchService.close()
                } catch (Throwable t) {
                    log.trace('failed stop watching directory', t)
                }
            }
        }
    }

    static class DirWatchJob implements Job {
        void execute(JobExecutionContext ctx) throws JobExecutionException {
            DirWatchService service = (DirWatchService)ctx.jobDetail.jobDataMap.service
            String savedName = Thread.currentThread().name
            Thread.currentThread().name = "$service.jexler.id|$service.id"
            for (WatchEvent watchEvent : service.watchKey.pollEvents()) {
                Path contextPath = ((Path) watchEvent.context())
                File file = new File(service.watchDir, contextPath.toFile().name)
                WatchEvent.Kind kind = watchEvent.kind()
                log.trace("event $kind '$file.absolutePath'")
                service.jexler.handle(new DirWatchEvent(service, file, kind))
            }
            Thread.currentThread().name = savedName
        }
    }

}
