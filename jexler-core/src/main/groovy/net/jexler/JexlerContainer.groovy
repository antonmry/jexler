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

package net.jexler

import net.jexler.service.Service
import net.jexler.service.ServiceGroup

import groovy.grape.Grape
import groovy.grape.GrapeEngine
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.quartz.Scheduler
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.simpl.RAMJobStore
import org.quartz.simpl.SimpleThreadPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Field

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

    /** Map of jexler ID to jexler. */
    private final Map<String,Jexler> jexlerMap

    private final IssueTracker issueTracker

    private Scheduler scheduler
    private final Object schedulerLock

    /**
     * Constructor from jexler script directory.
     * @param dir directory which contains jexler scripts
     * @throws RuntimeException if given dir is not a directory or does not exist
     */
    JexlerContainer(File dir) {
        // service ID is directory name
        super(dir.exists() ? dir.name : null)
        if (!dir.exists()) {
            throw new RuntimeException("Directory '$dir.absolutePath' does not exist.")
        } else  if (!dir.isDirectory()) {
            throw new RuntimeException("File '$dir.absolutePath' is not a directory.")
        }
        this.dir = dir
        jexlerMap = new TreeMap<>()
        issueTracker = new IssueTrackerBase()
        schedulerLock = new Object()
        WorkaroundGroovy7407.wrapGrapeEngineIfConfigured(this)
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
                    final String id = getJexlerId(file)
                    if (id != null && !jexlerMap.containsKey(id)) {
                        final Jexler jexler = new Jexler(file, this)
                        jexlerMap.put(jexler.id, jexler)
                    }
                }
            }

            // recreate list while omitting jexlers without script file that are stopped
            services.clear()
            jexlerMap.each { id, jexler ->
                if (jexler.file.exists() || jexler.state.on) {
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
    @Override
    void start() {
        for (Jexler jexler : jexlers) {
            if (jexler.metaConfig?.autostart) {
                jexler.start()
            }
        }
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
     * Get the list of all jexlers, first runnable jexlers,
     * then non-runnable ones, each group sorted by id.
     *
     * This is a copy, iterating over it can be freely done
     * and trying to add or remove list elements throws
     * an UnsupportedOperationException.
     */
    List<Jexler> getJexlers() {
        final List<Jexler> jexlers = new LinkedList<>()
        final List<Jexler> nonRunnables = new LinkedList<>()
        synchronized(jexlerMap) {
            for (Service service : services) {
                Jexler jexler = (Jexler)service
                if (jexler.runnable) {
                    jexlers.add(jexler)
                } else {
                    nonRunnables.add(jexler)
                }
            }
            jexlers.addAll(nonRunnables)
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
     * Get container directory.
     */
    File getDir() {
        return dir
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
        final String name = jexlerFile.name
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
                final String uuid = UUID.randomUUID()
                final String name = "JexlerContainerScheduler-$id-$uuid"
                final String instanceId = name
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
                scheduler.shutdown()
                scheduler = null
            }
        }
    }

    /**
     * Get logger for container.
     */
    static Logger getLogger() {
        return log
    }

    // Workaround for bug GROOVY-7407:
    //   "Compilation not thread safe if Grape / Ivy is used in Groovy scripts"
    //   https://issues.apache.org/jira/browse/GROOVY-7407
    @CompileStatic
    @PackageScope
    static class WorkaroundGroovy7407 {
        // boolean whether to wrap the GrapeEngine in the Grape class with a synchronized version
        public static final String GRAPE_ENGINE_WRAP_PROPERTY_NAME =
                'net.jexler.workaround.groovy.7407.grape.engine.wrap'
        public static final String LOG_PREFIX = 'workaround GROOVY-7407:'
        private static volatile Boolean isWrapGrapeEngine
        static void wrapGrapeEngineIfConfigured(JexlerContainer container) {
            if (isWrapGrapeEngine == null) {
                isWrapGrapeEngine = Boolean.valueOf(System.getProperty(GRAPE_ENGINE_WRAP_PROPERTY_NAME))
                if (isWrapGrapeEngine) {
                    log.trace("$LOG_PREFIX wrapping GrapeEngine...")
                    try {
                        WorkaroundGroovy7407WrappingGrapeEngine.createAndSet()
                        log.trace("$LOG_PREFIX successfully wrapped GrapeEngine")
                    } catch (Exception e) {
                        String msg = "failed to wrap GrapeEngine: $e"
                        log.trace("$LOG_PREFIX failed to wrap GrapeEngine: $e")
                        container.trackIssue(container, msg, e)
                    }

                }
            }
        }
        static void resetForUnitTests() {
            isWrapGrapeEngine = null
        }
    }

    /**
     * A GrapeEngine that wraps the current GrapeEngine with a wrapper where all calls
     * of the GrapeEngine API are synchronized with a configurable lock, and allows to
     * set this engine in the Grape class.
     *
     * Works at least in basic situations with Groovy 2.4.3 where the wrapped GrapeEngine
     * is always a GrapeIvy instance (not all public interface methods have been tested).
     *
     * But note that while a synchronized GrapeEngine call is in progress (which may take
     * a long time to complete, if e.g. downloading a JAR file from a maven repo),
     * all other threads that want to pull Grape dependencies must wait...
     *
     * Several things are not so nice about this approach:
     * - This is using a "trick" to set the static protected GrapeEngine instance in Grape;
     *   although nominally protected variables are part of the public API (and in this case
     *   is shown in the online JavaDoc of the Grape class).
     * - The "magic" with "calleeDepth" is based on exact knowledge of what GrapeIvy
     *   does (which, by the way, appears even inconsistent internally(?)), so this
     *   workaround is not guaranteed to be robust if GroovyIvy implementation changes.
     * - I refrained from referring to the GrapeIvy class in the source, because it is
     *   not publicly documented in the online JavaDoc of groovy-core.
     */
    @CompileStatic
    @PackageScope
    static class WorkaroundGroovy7407WrappingGrapeEngine implements GrapeEngine {

        private final Object lock
        private final GrapeEngine innerEngine

        // GrapeIvy.DEFAULT_DEPTH + 1, because is additionally wrapped by this class...
        private static final int DEFAULT_DEPTH = 4

        WorkaroundGroovy7407WrappingGrapeEngine(Object lock, GrapeEngine innerEngine) {
            this.lock = lock
            this.innerEngine = innerEngine
        }

        static void setEngine(GrapeEngine engine) throws Exception {
            final Field field = Grape.class.getDeclaredField('instance')
            field.accessible = true
            field.set(Grape.class, engine)
        }

        // call this somewhere during initialization to apply the workaround
        static void createAndSet() throws Exception {
            engine = new WorkaroundGroovy7407WrappingGrapeEngine(Grape.class, Grape.instance)
        }

        @Override
        Object grab(String endorsedModule) {
            synchronized(lock) {
                return innerEngine.grab(endorsedModule)
            }
        }

        @Override
        Object grab(Map args) {
            synchronized(lock) {
                if (args.get('calleeDepth') == null) {
                    args.put('calleeDepth', DEFAULT_DEPTH + 1)
                }
                return innerEngine.grab(args)
            }
        }

        @Override
        Object grab(Map args, Map... dependencies) {
            synchronized(lock) {
                if (args.get('calleeDepth') == null) {
                    args.put('calleeDepth', DEFAULT_DEPTH)
                }
                return innerEngine.grab(args, dependencies)
            }
        }

        @Override
        Map<String, Map<String, List<String>>> enumerateGrapes() {
            synchronized(lock) {
                return innerEngine.enumerateGrapes()
            }
        }

        @Override
        URI[] resolve(Map args, Map... dependencies) {
            synchronized(lock) {
                if (args.get('calleeDepth') == null) {
                    args.put('calleeDepth', DEFAULT_DEPTH)
                }
                return innerEngine.resolve(args, dependencies)
            }
        }

        @Override
        URI[] resolve(Map args, List depsInfo, Map... dependencies) {
            synchronized(lock) {
                return innerEngine.resolve(args, depsInfo, dependencies)
            }
        }

        @Override
        Map[] listDependencies(ClassLoader classLoader) {
            synchronized(lock) {
                return innerEngine.listDependencies(classLoader)
            }
        }

        @Override
        void addResolver(Map<String, Object> args) {
            synchronized(lock) {
                innerEngine.addResolver(args)
            }
        }
    }

}
