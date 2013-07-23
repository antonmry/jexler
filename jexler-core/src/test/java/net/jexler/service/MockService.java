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

import net.jexler.Jexler;
import net.jexler.RunState;


/**
 * Mock service implementation for unit tests.
 *
 * @author $(whois jexler.net)
 */
public final class MockService extends ServiceBase
{

	private static MockService instance;
	
	private volatile int nStarted = 0;
	private volatile int nStopped = 0;
	private volatile int nEventsSent = 0;
	private volatile int nEventsGotBack = 0;
	private RuntimeException stopRuntimeException = null;
	
	public static void setTestInstance(Jexler jexler, String id) {
		instance = new MockService(jexler, id);
	}

	public static MockService getTestInstance() {
		return instance;
	}
	
	public MockService(Jexler jexler, String id) {
		super(jexler,id);
	}

	@Override
	public void start() {
		nStarted++;
		this.setRunState(RunState.IDLE);
	}

	@Override
	public void stop() {
		nStopped++;
		if (stopRuntimeException != null) {
			throw stopRuntimeException;
		}
		this.setRunState(RunState.OFF);
	}
	
	public void setStopRuntimeException(RuntimeException e) {
		this.stopRuntimeException = e;
	}
	
	public RuntimeException getStopRuntimeException() {
		return stopRuntimeException;
	}
	
	public int getNStarted() {
		return nStarted;
	}
	
	public int getNStopped() {
		return nStopped;
	}
	
	public void notifyGotEvent() {
		nEventsGotBack++;
	}
	
	public int getNEventsGotBack() {
		return nEventsGotBack;
	}
	
	public void notifyJexler() {
		getJexler().handle(new MockEvent(this, "mock-event-id"));
		nEventsSent++;
	}
	
	public int getNEventsSent() {
		return nEventsSent;
	}
	

}
