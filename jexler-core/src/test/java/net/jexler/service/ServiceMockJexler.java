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

package net.jexler.service;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.jexler.Event;
import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.Metadata;
import net.jexler.RunState;

/**
 * Mock jexler for unit testing services.
 *
 * @author $(whois jexler.net)
 */
public class ServiceMockJexler implements Jexler {
	
	private Queue<Event> events;
	private File jexlerFile;
	private File jexlerDir;
	
	public ServiceMockJexler() {
		events = new ConcurrentLinkedQueue<Event>();
	}

	@Override
	public void start() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean isRunning() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void stop() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void trackIssue(Issue issue) {
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
	public RunState getRunState() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void waitForStartup(long timeout) {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public void handle(Event event) {
		events.add(event);
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

	@Override
	public void waitForShutdown(long timeout) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getId() {
		return "mockId";
	}

	
	public ServiceMockJexler setFile(File jexlerFile) {
		this.jexlerFile = jexlerFile;
		return this;
	}

	@Override
	public File getFile() {
		return jexlerFile;
	}
	
	public ServiceMockJexler setDir(File jexlerDir) {
		this.jexlerDir = jexlerDir;
		return this;
	}

	@Override
	public File getDir() {
		return jexlerDir;
	}

	@Override
	public Metadata getMetadata() {
		throw new RuntimeException("Not implemented");
	}

}
