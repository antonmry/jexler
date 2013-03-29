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

package net.jexler;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.jexler.service.StopService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jexler, runs a script that handles events.
 *
 * @author $(whois jexler.net)
 */
public class Jexler implements Service, IssueTracker {

    static final Logger log = LoggerFactory.getLogger(Jexler.class);
    
    @SuppressWarnings("serial")
    public class Services extends LinkedList<Service> {
        public void start() {
        	for (Service service : this) {
        		service.start();
        	}
        }
    }
    
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
    				trackIssue(new Issue(Jexler.this, "Could not take event.", e));
    			}
    		} while (true);
    	}
    }

    private File file;
    private String id;

    /**
     * Set to true just at the beginning of start() and set to false
     * once the script exits in any way, after it has been tried to
     * stop all sensors and actors.
     */
    private volatile boolean isRunning;
    private volatile RunState runState;
    private Thread scriptThread;
    private Events events;

    /**
     * List of services.
     * Scripts are free to add services to this list or not - if they do,
     * services are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private Services services;

    private StopService stopService;

    private List<Issue> issues;

    /**
     * Constructor.
     * @param file file with jexler script
     */
    public Jexler(File file) {
        this.file = file;
        id = getIdForFile(file);
        isRunning = false;
        runState = RunState.OFF;
        events = new Events();
        services = new Services();
        stopService = new StopService(this, "stop-jexler");
        issues = Collections.synchronizedList(new LinkedList<Issue>());
    }

    /**
     * Start if marked as autostart.
     */
    public Jexler autostart() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
           String line = reader.readLine();
           if (line != null && line.contains("autostart")) {
               start();
           }
        } catch (IOException e) {
            String msg = "Could not read file '" + file.getAbsolutePath() + "'.";
            trackIssue(new Issue(null, msg, e));
        }
        return this;
    }
    
    /**
     * Immediately sets isRunning to true, then tries to start the script.
     * Typically returns before the jexler script has started or completed
     * to initialize all of its services.
     * The jexler remains in the running state until the script exits in
     * any way, after it has been tried to stop all registered services
     * (sensors and actors).
     */
    @Override
    public void start() {
        log.info("*** Jexler start: " + id);
        if (isRunning) {
            return;
        }

        isRunning = true;
        runState = RunState.BUSY_STARTING;
        forgetIssues();
        services.add(stopService);

    	Binding binding = new Binding();
    	binding.setVariable("jexler", this);
    	binding.setVariable("events", events);
    	binding.setVariable("services", services);
    	binding.setVariable("log", log);
    	
    	final GroovyShell shell = new GroovyShell(binding);
    	shell.getClassLoader().addClasspath(file.getParent());

        scriptThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                        	shell.evaluate(file);
                        } catch (RuntimeException | IOException e) {
                        	trackIssue(new Issue(null, "Script failed.", e));
                        } finally {
                        	runState = RunState.BUSY_STOPPING;
                        	for (Service service : services) {
                        		try {
                        			service.stop();
                        		} catch (RuntimeException e) {
                        			trackIssue(new Issue(service, "Could not stop service.", e));
                        		}
                        	}
                        	events.clear();
                        	services.clear();
                        	isRunning = false;
                        	runState = RunState.OFF;
                        }
                    }
                });
        scriptThread.setDaemon(true);
        scriptThread.setName(id);
        scriptThread.start();
    }
    
    @Override
    public boolean isRunning() {
        return isRunning;
    }
    
    public RunState getRunState() {
    	return this.runState;
    }
    
    public void waitForStartup(long timeout) {
    	long t0 = System.currentTimeMillis();
    	do {
    		if (runState != RunState.BUSY_STARTING) {
    			return;
    		}
    		if (System.currentTimeMillis() - t0 > timeout) {
    			trackIssue(new Issue(this, "Timeout waiting for jexler startup.", null));
    			return;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
    }

    /**
     * Handle given event.
     * @param event
     */
    public void handle(Event event) {
        events.add(event);
    }

    /**
     * Sends stop event to jexler.
     */
    @Override
    public void stop() {
        log.info("*** Jexler stop: " + id);
        if (!isRunning) {
            return;
        }
        stopService.trigger();
    }
    
    public void waitForShutdown(long timeout) {
       	long t0 = System.currentTimeMillis();
    	do {
    		if (runState == RunState.OFF) {
    			return;
    		}
    		if (System.currentTimeMillis() - t0 > timeout) {
    			trackIssue(new Issue(this, "Timeout waiting for jexler shutdown.", null));
    			return;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
    }
      
    @Override
    public void addTo(List<Service> services) {
    	services.add(this);
    }

    @Override
    public void trackIssue(Issue issue) {
        log.error(issue.toString());
        issues.add(issue);
    }

    @Override
    public List<Issue> getIssues() {
        Collections.sort(issues);
        return issues;
    }

    @Override
    public void forgetIssues() {
        issues.clear();
    }

    /**
     * Get id.
     */
    public String getId() {
        return id;
    }

    /**
     * Get script file.
      */
    public File getFile() {
        return file;
    }
    
    /**
     * Get directory that contains script file.
      */
    public File getDir() {
        return file.getParentFile();
    }
    
    /**
     * Get jexler id for file or null if not a jexler script
     */
    public static String getIdForFile(File file) {
    	String name = file.getName();
    	String ext = ".groovy";
    	if (name.endsWith(ext)) {
    		return name.substring(0, name.length() - ext.length());
    	} else {
    		return null;
    	}
    }
    
    /**
     * Get file name for jexler id
     */
    public static String getFilenameForId(String id) {
    	String ext = ".groovy";
    	return id + ext;
    }

}
