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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jexler, runs a script that handles events.
 *
 * @author $(whois jexler.net)
 */
public class Jexler implements Service<Jexler>, IssueTracker {

    static final Logger log = LoggerFactory.getLogger(Jexler.class);

    private File file;
    private String id;

    /**
     * Set to true just at the beginning of start() and set to false
     * once the script exits in any way, after it has been tried to
     * stop all sensors and actors.
     */
    private volatile boolean isRunning;
    private Thread scriptThread;
    private BlockingQueue<Event> events;

    /**
     * List of services.
     * Scripts are free to add services to this list or not - if they do,
     * services are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private List<Service<?>> services;

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
        events = new LinkedBlockingQueue<Event>();
        services = new LinkedList<Service<?>>();
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
     * to initialize all its services.
     * The jexler remains in the running state until the script exits in
     * any way, after it has been tried to stop all registered services
     * (sensors and actors).
     *
     * LATER stash() method to put hanging jexlers away? and maybe try to
     * stop all services even when the script is still running?
     */
    @Override
    public Jexler start() {
        log.info("*** Jexler start: " + id);
        if (isRunning) {
            return this;
        }

        isRunning = true;

        services.add(stopService);

    	Binding binding = new Binding();
    	binding.setVariable("jexler", this);
    	binding.setVariable("file", file);
    	binding.setVariable("id", id);
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
                            // LATER use/log returned object?
                        	// LATER handle CompilationFailedException specifically?
                        } catch (RuntimeException | IOException e) {
                        	trackIssue(new Issue(null, "Script failed.", e));
                        } finally {
                        	for (Service<?> service : services) {
                        		try {
                        			service.stop(0);
                        		} catch (RuntimeException e) {
                        			trackIssue(new Issue(service, "Could not stop service.", e));
                        		}
                        	}
                        	events.clear();
                        	services.clear();
                        	isRunning = false;
                        }
                    }
                });
        scriptThread.setDaemon(true);
        scriptThread.setName(id);
        scriptThread.start();

        return this;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Handle given event.
     * @param event
     */
    public void handle(Event event) {
        events.add(event);
    }

    /**
     * Sends stop event to jexler and waits until stopped or timeout.
     */
    @Override
    public void stop(long timeout) {
        log.info("*** Jexler stop: " + id);
        if (!isRunning) {
            return;
        }

        stopService.trigger();

        long t0 = System.currentTimeMillis();
        while (isRunning && System.currentTimeMillis() - t0 < timeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
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
    
    /** Get jexler id for file or null if not a jexler script
     * @param file
     * @return
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


}
