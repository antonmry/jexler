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


/**
 * Run state of a service.
 *
 * @author $(whois jexler.net)
 */
public enum RunState  {

	OFF ("off"),
	BUSY_STARTING("busy (starting)"),
	IDLE("idle"),
	BUSY_EVENT("busy (event)"),
	BUSY_STOPPING("busy (stopping)");
	
	private final String info;
	
	RunState(String info) {
		this.info = info;
	}
	
	public String getInfo() {
		return info;
	}
		
	public boolean isOff() {
		return this == OFF;
	}
	
	public boolean isBusyStarting() {
		return this == BUSY_STARTING;
	}
	
	public boolean isIdle() {
		return this == IDLE;
	}
	
	public boolean isBusyEvent() {
		return this == BUSY_EVENT;
	}
		
	public boolean isBusyStopping() {
		return this == BUSY_STOPPING;
	}
	
	/**
	 * Convenience method, returns true if not OFF.
	 * @return true or false
	 */
	public boolean isOn() {
		return this != OFF;
	}
	
	/**
	 * Convenience method, returns true if IDLE or BUSY_EVENT,
	 * i.e. the service up and doing what it should do.
	 * @return true or false
	 */
	public boolean isOperational() {
		return this == IDLE || this == BUSY_EVENT;
	}
	
}

