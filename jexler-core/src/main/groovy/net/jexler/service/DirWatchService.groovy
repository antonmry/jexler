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
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

import net.jexler.Jexler
import net.jexler.RunState

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Directory watch service, creates an event when a file
 * in a given directory is created, modified oder deleted.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class DirWatchService extends ServiceBase {

    private static final Logger log = LoggerFactory.getLogger(DirWatchService.class)

    private File watchDir
    private Scheduler scheduler
    private String cron
    private TriggerKey triggerKey

    private WatchService watchService
    private WatchKey watchKey

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    DirWatchService(Jexler jexler, String id) {
        super(jexler, id)
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
     * Default is every minute, i.e. "* * * * *".
     * @return this (for chaining calls)
     */
    DirWatchService setCron(String cron) {
        this.cron = cron
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
        if (!off) {
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

        final DirWatchService thisService = this
        Class jobClass = ServiceUtil.newJobClassForRunnable(new Runnable() {
            void run() {
                String savedName = Thread.currentThread().name
                Thread.currentThread().name = "$jexler.id|$thisService.id"
                for (WatchEvent watchEvent : watchKey.pollEvents()) {
                    Path contextPath = ((Path) watchEvent.context())
                    File file = new File(watchDir, contextPath.toFile().name)
                    WatchEvent.Kind kind = watchEvent.kind()
                    log.trace("event $kind '$file.absolutePath'")
                    jexler.handle(new DirWatchEvent(thisService, file, kind))
                }
                Thread.currentThread().name = savedName
            }
        })

        String uuid = UUID.randomUUID()
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity("job-$id-$uuid", jexler.id)
                .build()
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-$id-$uuid", jexler.id)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build()
        triggerKey = trigger.key

        if (scheduler == null) {
            scheduler = jexler.container.scheduler
        }
        runState = RunState.IDLE
        scheduler.scheduleJob(job, trigger)
    }

    @Override
    void stop() {
        if (off) {
            return
        }
        scheduler.unscheduleJob(triggerKey)
        watchKey.cancel()
        try {
            watchService.close()
        } catch (IOException e) {
            log.trace('failed to close watch service', e)
        }
        runState = RunState.OFF
    }

}
