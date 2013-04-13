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
 * State of a running jexler.
 *
 * @author $(whois jexler.net)
 */
public enum RunState  {

	OFF ("off"),
	BUSY_STARTING("busy (starting)"),
	IDLE("idle"),
	BUSY_EVENT("busy (event)"),
	BUSY_STOPPING("busy (stopping)");
	
	private String info;
	
	RunState(String info) {
		this.info = info;
	}
	
	public String getInfo() {
		return info;
	}
}
