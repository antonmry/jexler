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

import net.jexler.RunState;


/**
 * Interface for a service.
 * Implemented by Jexler(s) themselves and services used by jexlers.
 * 
 * TODO: describe life cycle
 *
 * @author $(whois jexler.net)
 */
public interface Service {

    /**
     * Initiates service start.
     */
    void start();

    /**
     * Waits until run state is not BUSY_STARTING or timeout.
     * @param timeout timeout in seconds
     * @return true if no timeout, false otherwise
     */
    boolean waitForStartup(long timeout);


    /**
     * Initiates service stop.
     */
    void stop();
    
    /**
     * Waits until run state is not OFF or timeout.
     * @param timeout timeout in seconds
     * @return true if no timeout, false otherwise
     */
    boolean waitForShutdown(long timeout);

	/**
	 * Get run state of the service.
	 * @return
	 */
	RunState getRunState();    

    /**
     * Convenience method for testing if run state is not OFF.
     * @return true or false
     */
    boolean isOn();

    /**
     * Convenience method for testing  if run state is OFF.
     * @return true or false
     */
    boolean isOff();

    /**
     * Get id.
     * @return id
     */
    String getId();

}
