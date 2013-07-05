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

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.jexler.Event;
import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.Jexlers;
import net.jexler.MetaInfo;
import net.jexler.RunState;
import net.jexler.service.Service;

/**
 * Mock jexler implementation for unit tests.
 *
 * @author $(whois jexler.net)
 */
public class MockJexler implements Jexler {
	
	private File file;
	//private Jexlers jexlers;
	private Queue<Event> events;
	
	public MockJexler(File file, Jexlers jexlers) {
		this.file = file;
		//this.jexlers = jexlers;
		events = new ConcurrentLinkedQueue<Event>();
	}
	
	public MockJexler() {
		this(null, null);
	}

	@Override
	public void start() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean waitForStartup(long timeout) {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void stop() {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public boolean waitForShutdown(long timeout) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public RunState getRunState() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean isOn() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean isOff() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void trackIssue(Issue issue) {
		throw new RuntimeException("Not implemented");
	}

    @Override
    public void trackIssue(Service service, String message, Exception exception) {
    	throw new RuntimeException("Not implemented");
    }

    @Override
	public List<Issue> getIssues() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void forgetIssues() {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void handle(Event event) {
		events.add(event);
	}

	@Override
	public String getId() {
		return "mockId";
	}


	@Override
	public File getFile() {
		return file;
	}
	

	@Override
	public File getDir() {
		return (file == null) ? null : file.getParentFile();
	}

	@Override
	public MetaInfo getMetaInfo() {
		throw new RuntimeException("Not implemented");
	}
	
	/**
	 * Wait at most timeout ms for event, return
	 * event if got one in time, null otherwise.
	 */
	public Event takeEvent(long timeout) {
    	long t0 = System.currentTimeMillis();
    	do {
    		Event event = events.poll();
    		if (event != null) {
    			return event;
    		}
    		if (System.currentTimeMillis() - t0 > timeout) {
    			return null;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
	}

}
