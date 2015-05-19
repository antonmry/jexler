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

package net.jexler

import groovy.transform.CompileStatic

import net.jexler.service.ServiceGroup
import net.jexler.service.Service
import org.quartz.Scheduler
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.simpl.RAMJobStore
import org.quartz.simpl.SimpleThreadPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Container of all jexlers in a directory.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerContainer extends ServiceGroup implements Service, IssueTracker, Closeable {

    private static final Logger log = LoggerFactory.getLogger(JexlerContainer.class)

    private static final String EXT = '.groovy'

    private final File dir
    private final String id

    /** key is jexler id */
    private final Map<String,Jexler> jexlerMap

    private final IssueTracker issueTracker

    private Scheduler scheduler
    private final Object schedulerLock

    /**
     * Constructor.
     * @param dir directory which contains jexler scripts
     * @throws RuntimeException if given dir is not a directory or does not exist
     */
    JexlerContainer(File dir) {
        super(dir.exists() ? dir.name : null)
        if (!dir.exists()) {
            throw new RuntimeException("Directory '$dir.absolutePath' does not exist.")
        } else  if (!dir.isDirectory()) {
            throw new RuntimeException("File '$dir.absolutePath' is not a directory.")
        }
        this.dir = dir
        id = super.id
        jexlerMap = new TreeMap<>()
        issueTracker = new IssueTrackerBase()
        schedulerLock = new Object()
        refresh()
    }

    /**
     * Refresh list of jexlers.
     * Add new jexlers for new script files;
     * remove old jexlers if their script file is gone and they are stopped.
     */
    void refresh() {
        synchronized (jexlerMap) {
            // list directory and create jexlers in map for new script files in directory
            dir.listFiles()?.each { File file ->
                if (file.isFile() && !file.isHidden()) {
                    String id = getJexlerId(file)
                    if (id != null && !jexlerMap.containsKey(id)) {
                        Jexler jexler = new Jexler(file, this)
                        jexlerMap.put(jexler.id, jexler)
                    }
                }
            }

            // recreate list while omitting jexlers without script file that are stopped
            services.clear()
            jexlerMap.each { id, jexler ->
                if (jexler.file.exists() || jexler.on) {
                    services.add(jexler)
                }
            }

            // recreate map with list entries
            jexlerMap.clear()
            for (Jexler jexler : jexlers) {
                jexlerMap.put(jexler.id, jexler)
            }
        }
    }

    /**
     * Start jexlers that are marked as autostart.
     */
    void autostart() {
        for (Jexler jexler : jexlers) {
            if (jexler.metaInfo.autostart) {
                jexler.start()
            }
        }
    }

    @Override
    boolean waitForStartup(long timeout) {
        boolean ok = super.waitForStartup(timeout)
        if (!ok) {
            for (Jexler jexler : jexlers) {
                if (jexler.runState == RunState.BUSY_STARTING) {
                    trackIssue(jexler, 'Timeout waiting for jexler startup.', null)
                }
            }
        }
        return ok
    }

    @Override
    boolean waitForShutdown(long timeout) {
        boolean ok = super.waitForShutdown(timeout)
        if (!ok) {
            for (Jexler jexler : jexlers) {
                if (jexler.runState != RunState.OFF) {
                    trackIssue(jexler, 'Timeout waiting for jexler shutdown.', null)
                }
            }
        }
        return ok
    }

    @Override
    void trackIssue(Issue issue) {
        issueTracker.trackIssue(issue)
    }

    @Override
    void trackIssue(Service service, String message, Throwable cause) {
        issueTracker.trackIssue(service, message, cause)
    }

    @Override
    List<Issue> getIssues() {
        return issueTracker.issues
    }

    @Override
    void forgetIssues() {
        issueTracker.forgetIssues()
    }

    /**
     * Get id, which is the name of the jexler container directory.
     */
    @Override
    String getId() {
        return id
    }

    /**
     * Get directory that contains the jexler Groovy scripts.
     */
    File getDir() {
        return dir
    }

    /**
     * Get the list of all jexlers, sorted by id.
     *
     * This is a copy, iterating over it can be freely done
     * and trying to add or remove list elements throws
     * an UnsupportedOperationException.
     */
    List<Jexler> getJexlers() {
        List<Jexler> jexlers = new LinkedList<>()
        synchronized(jexlerMap) {
            jexlers.addAll((List<Jexler>)(List)services)
        }
        return Collections.unmodifiableList(jexlers)
    }

    /**
     * Get the jexler for the given id.
     * @return jexler for given id or null if none
     */
    Jexler getJexler(String id) {
        synchronized(jexlerMap) {
            return jexlerMap.get(id)
        }
    }

    /**
     * Get the file for the given jexler id,
     * even if no such file exists (yet).
     */
    File getJexlerFile(String id) {
        return new File(dir, "$id$EXT")
    }

    /**
     * Get the jexler id for the given file,
     * even if the file does not exist (any more),
     * or null if not a jexler script.
     */
    String getJexlerId(File jexlerFile) {
        String name = jexlerFile.name
        if (name.endsWith(EXT)) {
            return name.substring(0, name.length() - EXT.length())
        } else {
            return null
        }
    }

    /**
     * Get shared quartz scheduler, already started.
     */
    Scheduler getScheduler() {
        synchronized (schedulerLock) {
            if (scheduler == null) {
                String uuid = UUID.randomUUID()
                String name = "JexlerContainerScheduler-$id-$uuid"
                String instanceId = name
                DirectSchedulerFactory.getInstance().createScheduler(name, instanceId,
                        new SimpleThreadPool(5, Thread.currentThread().priority), new RAMJobStore())
                scheduler = DirectSchedulerFactory.getInstance().getScheduler(name)
                scheduler.start()
            }
            return scheduler
        }
    }

    /**
     * Stop the shared quartz scheduler, plus close maybe other things.
     */
    void close() {
        synchronized (schedulerLock) {
            if (scheduler != null) {
                DirectSchedulerFactory.getInstance().allSchedulers
                scheduler.shutdown()
                scheduler = null
            }
        }
    }

}
