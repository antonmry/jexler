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
 * Interface for a service.
 * Implemented by Jexler(s) themselves and services used by jexlers.
 *
 * @author $(whois jexler.net)
 */
public interface Service {

    /**
     * Initiates service start.
     */
    void start();

    /**
     * Get info if service is running or not.
     * Service is running between sometime during start()
     * and sometime during or after stop().
     * @return true or false
     */
    boolean isRunning();

    /**
     * Initiates service stop.
     */
    void stop();

    /**
     * Get id.
     * @return id
     */
    String getId();

}
