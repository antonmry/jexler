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

import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.JexlerFactory;
import net.jexler.Jexlers;
import net.jexler.RunState;
import net.jexler.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock jexlers implementation.
 *
 * @author $(whois jexler.net)
 */
public class MockJexlers implements Jexlers {

	static final Logger log = LoggerFactory.getLogger(MockJexlers.class);

    /**
     * Constructor.
     * @param dir directory which contains jexler scripts
     */
    public MockJexlers(File dir, JexlerFactory jexlerFactory) {
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
	public void refresh() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void autostart() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getId() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public File getDir() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public List<Jexler> getJexlers() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Jexler getJexler(String id) {
		throw new RuntimeException("Not implemented");
	}

}
