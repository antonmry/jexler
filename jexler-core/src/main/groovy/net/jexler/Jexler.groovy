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

import java.util.concurrent.LinkedBlockingQueue

import groovy.grape.Grape
import groovy.grape.GrapeEngine
import net.jexler.service.BasicServiceGroup
import net.jexler.service.Event
import net.jexler.service.Service
import net.jexler.service.ServiceGroup
import net.jexler.service.ServiceUtil
import net.jexler.service.StopEvent
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Jexler, runs a Groovy script that handles events.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class Jexler implements Service, IssueTracker {

    private static final Logger log = LoggerFactory.getLogger(Jexler.class)

    @SuppressWarnings("serial")
    @CompileStatic
    class Events extends LinkedBlockingQueue<Event> {
        @Override
        Event take() {
            runState = RunState.IDLE
            while (true) {
                try {
                    Event event = super.take()
                    runState = RunState.BUSY_EVENT
                    return event
                } catch (InterruptedException e) {
                    trackIssue(Jexler.this, "Could not take event.", e)
                }
            }
        }
    }

    private final File file
    private final String id
    private final JexlerContainer container
    private volatile RunState runState
    private final Events events

    /**
     * Group of services.
     * Scripts are free to add services to this list or not - if they do,
     * services are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private final ServiceGroup services

    private final IssueTracker issueTracker

    private Map<String,Object> metaInfoAtStart

    /**
     * Constructor.
     * @param file file with jexler script
     * @param container jexler container that contains this jexler
     */
    Jexler(File file, JexlerContainer container) {
        this.file = file
        this.container = container
        id = container.getJexlerId(file)
        runState = RunState.OFF
        events = new Events()
        services = new BasicServiceGroup(id + ".services")
        issueTracker = new BasicIssueTracker()
    }

    /**
     * Initiate jexler start.
     * Immediately marks the jexler service as starting up, then tries to
     * start the script.
     * Typically returns before the jexler script has started or completed
     * to initialize all of its services.
     * The jexler remains in the running state until the script exits in
     * any way, after it has been tried to stop all registered services
     * (sensors and actors).
     */
    @Override
    void start() {
        log.info("*** Jexler start: " + id)
        if (isOn()) {
            return
        }
        runState = RunState.BUSY_STARTING

        forgetIssues()

        metaInfoAtStart = readMetaInfo()
        if (!getIssues().isEmpty()) {
            runState = RunState.OFF
            return
        }

        // prepare for compile
        WorkaroundGroovy7407.wrapGrapeEngineIfConfigured()
        final CompilerConfiguration config = new CompilerConfiguration()
        if (JexlerUtil.isMetaInfoOn(getMetaInfo(), "autoimport", true)) {
            ImportCustomizer importCustomizer = new ImportCustomizer()
            importCustomizer.addStarImports(
                    "net.jexler", "net.jexler.service", "net.jexler.tool")
            config.addCompilationCustomizers(importCustomizer)
        }
        final GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config)
        loader.addClasspath(file.getParent())

        // compile
        final Class<?> clazz
        try {
            clazz = loader.parseClass(file)
        } catch (Throwable t) {
            // (may throw almost anything, checked or not)
            trackIssue(this, "Script compile failed.", t)
            runState = RunState.OFF
            return
        }

        // not a runnable script?
        if (!Script.class.isAssignableFrom(clazz)) {
            runState = RunState.OFF
            return
        }

        // create script and run in a separate thread
        final Jexler thisJexler = this
        Thread scriptThread = new Thread(
                new Runnable() {
                    void run() {
                        // create script instance
                        final Script script
                        try {
                            script = (Script)clazz.newInstance()
                        } catch (Throwable t) {
                            // (may throw anything, checked or not)
                            trackIssue(thisJexler, "Script create failed.", t)
                            runState = RunState.OFF
                            return
                        }

                        // run script
                        final Binding binding = new Binding()
                        binding.setVariable("jexler", thisJexler)
                        binding.setVariable("container", container)
                        binding.setVariable("events", events)
                        binding.setVariable("services", services)
                        binding.setVariable("log", log)
                        script.setBinding(binding)
                        try {
                            script.run()
                        } catch (Throwable t) {
                            // (script may throw anything, checked or not)
                            trackIssue(thisJexler, "Script run failed.", t)
                        }

                        runState = RunState.BUSY_STOPPING

                        try {
                            services.stop()
                        } catch (Throwable t) {
                            trackIssue(services, "Could not stop services.", t)
                        }
                        events.clear()
                        services.getServices().clear()

                        runState = RunState.OFF
                    }
                })
        scriptThread.setDaemon(true)
        scriptThread.setName(id)
        scriptThread.start()
    }

    @Override
    boolean waitForStartup(long timeout) {
        boolean ok = ServiceUtil.waitForStartup(this, timeout)
        if (!ok) {
            trackIssue(this, "Timeout waiting for jexler startup.", null)
        }
        return ok
    }

    /**
     * Handle given event.
     */
    void handle(Event event) {
        events.add(event)
    }

    /**
     * Initiate jexler stop by sending it a stop event to handle.
     */
    @Override
    void stop() {
        log.info("*** Jexler stop: " + id)
        if (isOff()) {
            return
        }
        handle(new StopEvent(this))
    }

    @Override
    boolean waitForShutdown(long timeout) {
        boolean ok = ServiceUtil.waitForShutdown(this, timeout)
        if (!ok) {
            trackIssue(this, "Timeout waiting for jexler shutdown.", null)
        }
        return ok
    }

    @Override
    RunState getRunState() {
        return runState
    }

    @Override
    boolean isOn() {
        return runState.isOn()
    }

    @Override
    boolean isOff() {
        return runState.isOff()
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
        return issueTracker.getIssues()
    }

    @Override
    void forgetIssues() {
        issueTracker.forgetIssues()
    }

    @Override
    String getId() {
        return id
    }

    /**
     * Get script file.
     */
    File getFile() {
        return file
    }

    /**
     * Get directory that contains script file.
     */
    File getDir() {
        return file.getParentFile()
    }

    /**
     * Get meta info.
     *
     * Read from the jexler file at each call except if the jexler
     * is already running, in that case returns meta info read at
     * the time the jexler was started.
     *
     * The meta info of a jexler is stored in the first line of
     * a jexler script file as a map in Groovy notation.
     *
     * Example:
     * <pre>
     *
     * [ "autostart" : true, "root" : new File('/') ]
     * </pre>
     *
     * Meta info is silently considered empty if for some reason
     * evaluating the line is not possible or fails or or evaluates
     * to an object which is not a map.
     */
    Map<String,Object> getMetaInfo() {
        if (isOn()) {
            return metaInfoAtStart
        } else {
            return readMetaInfo()
        }
    }

    private Map<String,Object> readMetaInfo() {
        Map<String,Object> info = new HashMap<>()

        if (!file.exists()) {
            return info
        }

        List<String> lines
        try {
            lines = file.readLines()
        } catch (IOException e) {
            String msg = "Could not read meta info from jexler file '${file.absolutePath}'."
            trackIssue(this, msg, e)
            return info
        }

        if (lines.isEmpty()) {
            return info
        }
        String line = lines.first()

        WorkaroundGroovy7407.wrapGrapeEngineIfConfigured()

        // evaluate first line as groovy script
        Object obj
        try {
            obj = new GroovyShell().evaluate(line)
        } catch (Throwable t) {
            // (script may throw anything, checked or not)
            return info
        }

        // evaluated to a map?
        if (obj == null || !(obj instanceof Map)) {
            return info
        }

        // set map
        @SuppressWarnings("unchecked")
        Map<String,Object> map = (Map<String,Object>)obj
        info.putAll(map)

        return info
    }

    /**
     * Get the container that contains this jexler.
     */
    JexlerContainer getContainer() {
        return container
    }

    // Workaround for bug GROOVY-7407:
    //   "Compilation not thread safe if Grape / Ivy is used in Groovy scripts"
    //   https://issues.apache.org/jira/browse/GROOVY-7407
    static class WorkaroundGroovy7407 {
        // boolean whether to wrap the GrapeEngine in the Grape class with a synchronized version
        public static final String GRAPE_ENGINE_WRAP_PROPERTY_NAME = "net.jexler.workaround.groovy.7407.grape.engine.wrap"
        public static final String LOG_PREFIX = "workaround GROOVY-7407: "
        private static volatile Boolean isWrapGrapeEngine
        static void wrapGrapeEngineIfConfigured() {
            if (isWrapGrapeEngine == null) {
                isWrapGrapeEngine = Boolean.valueOf(System.getProperty(GRAPE_ENGINE_WRAP_PROPERTY_NAME))
                if (isWrapGrapeEngine) {
                    log.trace(LOG_PREFIX + "wrapping GrapeEngine...")
                    WorkaroundGroovy7407WrappingGrapeEngine.createAndSet()
                    log.trace(LOG_PREFIX + "successfully wrapped GrapeEngine")
                }
            }
        }
        static void resetForUnitTests() {
            isWrapGrapeEngine = null
        }
    }

}
