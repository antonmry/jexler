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

package net.jexler.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.jexler.Event;
import net.jexler.Issue;
import net.jexler.IssueTracker;
import net.jexler.Jexler;
import net.jexler.Jexlers;
import net.jexler.MetaInfo;
import net.jexler.RunState;
import net.jexler.service.BasicServiceGroup;
import net.jexler.service.Service;
import net.jexler.service.ServiceGroup;
import net.jexler.service.ServiceUtil;
import net.jexler.service.StopService;

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

    static final Logger log = LoggerFactory.getLogger(BasicJexler.class);
        
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
    private Thread scriptThread;
    
    private final Events events;

    /**
     * Group of services.
     * Scripts are free to add services to this list or not - if they do,
     * services are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private final ServiceGroup services;

    private final StopService stopService;

    private final IssueTracker issueTracker;
    
    private MetaInfo metaInfoAtStart;

    /**
     * Constructor.
     * @param file file with jexler script
     * @param jexlers
     */
    public BasicJexler(File file, Jexlers jexlers) {
        this.file = file;
        this.jexlers = jexlers;
        id = JexlerUtil.getJexlerIdForFile(file);
        runState = RunState.OFF;
        events = new Events();
        services = new BasicServiceGroup(id + ".services");
        stopService = new StopService(this, "stop-jexler");
        issueTracker = new BasicIssueTracker();
    }

    /**
     * Immediately marks jexler service as on, then tries to start the script.
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
        services.add(stopService);
        
    	final Binding binding = new Binding();
    	binding.setVariable("jexler", this);
    	binding.setVariable("jexlers", jexlers);
    	binding.setVariable("events", events);
    	binding.setVariable("services", services);
    	binding.setVariable("log", log);
    	
    	ImportCustomizer importCustomizer = new ImportCustomizer();
    	if (metaInfoAtStart.isOn("autoimport", true)) {
    		importCustomizer.addStarImports(
    				"net.jexler", "net.jexler.service", "net.jexler.tool");
    	}
    	CompilerConfiguration configuration = new CompilerConfiguration();
    	configuration.addCompilationCustomizers(importCustomizer);

    	final GroovyShell shell = new GroovyShell(binding, configuration);
    	shell.getClassLoader().addClasspath(file.getParent());

        scriptThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                        	shell.evaluate(file);
                        } catch (RuntimeException | IOException e) {
                        	trackIssue(null, "Script failed.", e);
                        } finally {
                        	runState = RunState.BUSY_STOPPING;
                        	try {
                        		services.stop();
                        	} catch (RuntimeException e) {
                        		trackIssue(services, "Could not stop services.", e);
                        	}
                        	events.clear();
                        	services.getServiceList().clear();
                        	runState = RunState.OFF;
                        }
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
     * Sends stop event to jexler.
     */
    @Override
    public void stop() {
        log.info("*** Jexler stop: " + id);
        if (isOff()) {
            return;
        }
        stopService.trigger();
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
    public void trackIssue(Service service, String message, Exception exception) {
    	issueTracker.trackIssue(service, message, exception);
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
     * Read meta info from jexler file an store it in member variable.
     */
    private void setMetaInfoAtStart() {
    	try {
    		metaInfoAtStart = new BasicMetaInfo(file);
    	} catch (IOException e) {
    		String msg = "Could not read meta info from existing jexler file '" 
    				+ file.getAbsolutePath() + "'.";
    		trackIssue(null, msg, e);
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
    			String msg = "Could not read meta info from existing jexler file '" 
    					+ file.getAbsolutePath() + "'.";
    			trackIssue(null, msg, e);
    			return BasicMetaInfo.EMPTY;
    		}
    	}
    }

}
