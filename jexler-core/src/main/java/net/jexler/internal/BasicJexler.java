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

package net.jexler.internal;

import groovy.grape.Grape;
import groovy.grape.GrapeEngine;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import groovy.lang.Script;
import net.jexler.Issue;
import net.jexler.IssueTracker;
import net.jexler.Jexler;
import net.jexler.JexlerUtil;
import net.jexler.Jexlers;
import net.jexler.MetaInfo;
import net.jexler.RunState;
import net.jexler.service.BasicServiceGroup;
import net.jexler.service.Event;
import net.jexler.service.Service;
import net.jexler.service.ServiceGroup;
import net.jexler.service.ServiceUtil;
import net.jexler.service.StopEvent;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic default implementation of jexler interface.
 *
 * @author $(whois jexler.net)
 */
public class BasicJexler implements Jexler {

    private static final Logger log = LoggerFactory.getLogger(BasicJexler.class);

    @SuppressWarnings("serial")
	public class Events extends LinkedBlockingQueue<Event> {
    	@Override
    	public Event take() {
    		runState = RunState.IDLE;
    		do {
    			try {
    				Event event = super.take();
    				runState = RunState.BUSY_EVENT;
    				return event;
    			} catch (InterruptedException e) {
    				trackIssue(BasicJexler.this, "Could not take event.", e);
    			}
    		} while (true);
    	}
    }

    private final File file;
    private final Jexlers jexlers;
    private final String id;

    private volatile RunState runState;
    
    private final Events events;

    /**
     * Group of services.
     * Scripts are free to add services to this list or not - if they do,
     * services are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private final ServiceGroup services;

    private final IssueTracker issueTracker;
    
    private MetaInfo metaInfoAtStart;

    /**
     * Constructor.
     * @param file file with jexler script
     * @param jexlers jexlers instance that contains this jexler
     */
    public BasicJexler(File file, Jexlers jexlers) {
        this.file = file;
        this.jexlers = jexlers;
        id = jexlers.getJexlerId(file);
        runState = RunState.OFF;
        events = new Events();
        services = new BasicServiceGroup(id + ".services");
        issueTracker = new BasicIssueTracker();
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
    public void start() {
        log.info("*** Jexler start: " + id);
        if (isOn()) {
            return;
        }

        forgetIssues();
        setMetaInfoAtStart();
        runState = RunState.BUSY_STARTING;

        BasicJexler.WorkaroundGroovy7407.wrapGrapeEngineIfConfigured();

    	final Binding binding = new Binding();
    	binding.setVariable("jexler", this);
    	binding.setVariable("jexlers", jexlers);
    	binding.setVariable("events", events);
    	binding.setVariable("services", services);
    	binding.setVariable("log", log);
    	
    	ImportCustomizer importCustomizer = new ImportCustomizer();
    	if (getMetaInfo().isOn("autoimport", true)) {
    		importCustomizer.addStarImports(
    				"net.jexler", "net.jexler.service", "net.jexler.tool");
    	}
    	final CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(importCustomizer);

        final ClassLoader parent = Thread.currentThread().getContextClassLoader();
    	
    	final Jexler thisJexler = this;

        Thread scriptThread = new Thread(
                new Runnable() {

                    public void run() {
                        try {
                            Class<?> clazz;
                            if (WorkaroundGroovy7407.getNumberOfRetries() == 0) {
                                clazz = parse();
                            } else {
                                clazz = parseWithRetries();
                            }
                            if (Script.class.isAssignableFrom(clazz)) {
                                Script script = (Script)clazz.newInstance();
                                script.setBinding(binding);
                                script.run();
                            }
                        } catch (Throwable t) {
                            // (script may throw anything, checked or not)
                            trackIssue(thisJexler, "Script failed.", t);
                        } finally {
                        	runState = RunState.BUSY_STOPPING;
                        	try {
                        		services.stop();
                        	} catch (RuntimeException e) {
                        		trackIssue(services, "Could not stop services.", e);
                        	}
                        	events.clear();
                        	services.getServices().clear();
                        	runState = RunState.OFF;
                        }
                    }

                    private Class<?> parse() throws IOException {
                        GroovyClassLoader loader = new GroovyClassLoader(parent, config);
                        loader.addClasspath(file.getParent());
                        return loader.parseClass(file);
                    }

                    private Class<?> parseWithRetries() throws Throwable {
                        Class<?> clazz = null;
                        synchronized (Jexler.class) {
                            try {
                                clazz = parse();
                            } catch (Throwable t) {
                                log.trace(WorkaroundGroovy7407.LOG_PREFIX + "compile failed: "
                                        + JexlerUtil.toSingleLine(JexlerUtil.getStackTrace(t)));
                                int n = WorkaroundGroovy7407.getNumberOfRetries();
                                boolean recovered = false;
                                for (int i = 1; i <= n; i++) {
                                    try {
                                        clazz = parse();
                                        log.trace(WorkaroundGroovy7407.LOG_PREFIX + "compile retry " + i + " was successful");
                                        recovered = true;
                                        if (WorkaroundGroovy7407.isReportRetries()) {
                                            jexlers.trackIssue(thisJexler,
                                                    WorkaroundGroovy7407.LOG_PREFIX + "recovered at retry " + i, t);
                                        }
                                        break;
                                    } catch (Throwable t2) {
                                        t = t2;
                                        log.trace(WorkaroundGroovy7407.LOG_PREFIX + "compile retry " + i + " failed: "
                                                + JexlerUtil.toSingleLine(JexlerUtil.getStackTrace(t)));
                                    }
                                }
                                if (!recovered) {
                                    throw t;
                                }
                            }
                        }
                        return clazz;
                    }

                });
        scriptThread.setDaemon(true);
        scriptThread.setName(id);
        scriptThread.start();
    }
        
    @Override
    public boolean waitForStartup(long timeout) {
    	boolean ok = ServiceUtil.waitForStartup(this, timeout);
    	if (!ok) {
    		trackIssue(this, "Timeout waiting for jexler startup.", null);
    	}
    	return ok;
    }

    @Override
    public void handle(Event event) {
        events.add(event);
    }

    /**
     * Initiate jexler stop by sending it a stop event to handle.
     */
    @Override
    public void stop() {
        log.info("*** Jexler stop: " + id);
        if (isOff()) {
            return;
        }
        handle(new StopEvent(this));
    }
    
    @Override
    public boolean waitForShutdown(long timeout) {
    	boolean ok = ServiceUtil.waitForShutdown(this, timeout);
    	if (!ok) {
    		trackIssue(this, "Timeout waiting for jexler shutdown.", null);
    	}
    	return ok;
    }

    @Override
    public RunState getRunState() {
        return runState;
    }

    @Override
    public boolean isOn() {
        return runState.isOn();
    }

    @Override
    public boolean isOff() {
        return runState.isOff();
    }

    @Override
    public void trackIssue(Issue issue) {
        issueTracker.trackIssue(issue);
    }

    @Override
    public void trackIssue(Service service, String message, Throwable cause) {
    	issueTracker.trackIssue(service, message, cause);
    }

    @Override
    public List<Issue> getIssues() {
        return issueTracker.getIssues();
    }

    @Override
    public void forgetIssues() {
    	issueTracker.forgetIssues();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public File getFile() {
        return file;
    }
    
    @Override
    public File getDir() {
        return file.getParentFile();
    }
    
    /**
     * Read meta info from jexler file and store it in member variable.
     */
    private void setMetaInfoAtStart() {
    	try {
    		metaInfoAtStart = new BasicMetaInfo(file);
    	} catch (IOException e) {
    		String msg = "Could not read meta info from jexler file '" 
    				+ file.getAbsolutePath() + "'.";
    		trackIssue(this, msg, e);
    		metaInfoAtStart = BasicMetaInfo.EMPTY;
    	}
    }
    
    @Override
    public MetaInfo getMetaInfo() {
    	if (isOn()) {
    		return metaInfoAtStart;
    	} else {
    		try {
    			return new BasicMetaInfo(file);
    		} catch (IOException e) {
    			String msg = "Could not read meta info from jexler file '" 
    					+ file.getAbsolutePath() + "'.";
    			trackIssue(this, msg, e);
    			return BasicMetaInfo.EMPTY;
    		}
    	}
    }

    // partial(!) workarounds for bug GROOVY-7407:
    //   "Compilation not thread safe if Grape / Ivy is used in Groovy scripts"
    //   https://issues.apache.org/jira/browse/GROOVY-7407
    static class WorkaroundGroovy7407 {
        // number of compile retries
        static final String RETRIES_PROPERTY_NAME = "net.jexler.workaround.groovy.7407.retries";
        // boolean whether to create a jexlers issue if compile retries were necessary
        static final String RETRIES_REPORT_PROPERTY_NAME = "net.jexler.workaround.groovy.7407.retries.report";
        // boolean whether to wrap the GrapeEngine in the Grape class with a synchronized version
        static final String GRAPE_ENGINE_WRAP_PROPERTY_NAME = "net.jexler.workaround.groovy.7407.grape.engine.wrap";
        static final String LOG_PREFIX = "workaround GROOVY-7407: ";
        private static volatile Integer numberOfRetries;
        private static volatile Boolean isReportRetries;
        private static volatile Boolean isWrapGrapeEngine;

        static int getNumberOfRetries() {
            if (numberOfRetries == null) {
                String value = System.getProperty(RETRIES_PROPERTY_NAME, "0");
                try {
                    numberOfRetries = Integer.parseInt(value);
                    if (numberOfRetries != 0) {
                        log.trace(LOG_PREFIX + "is active, number of compile retries: " + numberOfRetries);
                        log.trace(LOG_PREFIX + "reporting compile retries as jexlers issues: " + isReportRetries());
                    }
                } catch (NumberFormatException e) {
                    log.trace(LOG_PREFIX + "property value '" + value + "' is not a number");
                    numberOfRetries = 0;
                }
            }
            return numberOfRetries;
        }

        static boolean isReportRetries() {
            if (isReportRetries == null) {
                isReportRetries = Boolean.valueOf(System.getProperty(RETRIES_REPORT_PROPERTY_NAME));
            }
            return isReportRetries;
        }

        static void resetForUnitTests() {
            numberOfRetries = null;
            isReportRetries = null;
            isWrapGrapeEngine = null;
        }

        static void wrapGrapeEngineIfConfigured() {
            if (isWrapGrapeEngine == null) {
                isWrapGrapeEngine = Boolean.valueOf(System.getProperty(GRAPE_ENGINE_WRAP_PROPERTY_NAME));
                if (isWrapGrapeEngine) {
                    log.trace(LOG_PREFIX + "wrapping GrapeEngine...");
                    WorkaroundGroovy7407WrappingGrapeEngine.createAndSet();
                    log.trace(LOG_PREFIX + "successfully wrapped GrapeEngine");
                }
            }
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
    static class WorkaroundGroovy7407WrappingGrapeEngine implements GrapeEngine {

        private final Object lock;
        private final GrapeEngine innerEngine;

        // GrapeIvy.DEFAULT_DEPTH + 1, because is additionally wrapped by this class...
        private static final int DEFAULT_DEPTH = 4;

        public WorkaroundGroovy7407WrappingGrapeEngine(Object lock, GrapeEngine innerEngine) {
            this.lock = lock;
            this.innerEngine = innerEngine;
        }

        public static void setEngine(GrapeEngine engine) {
            new Grape() {
                public void setInstance(GrapeEngine engine) {
                    synchronized (Grape.class) {
                        Grape.instance = engine;
                    }
                }
            }.setInstance(engine);
        }

        // call this somewhere during initialization to apply the workaround
        public static void createAndSet() {
            setEngine(new WorkaroundGroovy7407WrappingGrapeEngine(Grape.class, Grape.getInstance()));
        }

        @Override
        public Object grab(String endorsedModule) {
            synchronized(lock) {
                return innerEngine.grab(endorsedModule);
            }
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object grab(Map args) {
            synchronized(lock) {
                if (args.get("calleeDepth") == null) {
                    args.put("calleeDepth", DEFAULT_DEPTH + 1);
                }
                return innerEngine.grab(args);
            }
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object grab(Map args, Map... dependencies) {
            synchronized(lock) {
                if (args.get("calleeDepth") == null) {
                    args.put("calleeDepth", DEFAULT_DEPTH);
                }
                return innerEngine.grab(args, dependencies);
            }
        }

        @Override
        public Map<String, Map<String, List<String>>> enumerateGrapes() {
            synchronized(lock) {
                return innerEngine.enumerateGrapes();
            }
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public URI[] resolve(Map args, Map... dependencies) {
            synchronized(lock) {
                if (args.get("calleeDepth") == null) {
                    args.put("calleeDepth", DEFAULT_DEPTH);
                }
                return innerEngine.resolve(args, dependencies);
            }
        }

        @Override
        @SuppressWarnings("rawtypes")
        public URI[] resolve(Map args, List depsInfo, Map... dependencies) {
            synchronized(lock) {
                return innerEngine.resolve(args, depsInfo, dependencies);
            }
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Map[] listDependencies(ClassLoader classLoader) {
            synchronized(lock) {
                return innerEngine.listDependencies(classLoader);
            }
        }

        @Override
        public void addResolver(Map<String, Object> args) {
            synchronized(lock) {
                innerEngine.addResolver(args);
            }
        }
    }

}
