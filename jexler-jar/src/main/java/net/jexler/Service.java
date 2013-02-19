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
 * Implemented by Jexler(s) themselves and various services used by jexlers.
 *
 * @author $(whois jexler.net)
 */
public interface Service<T> {

    /**
     * Start service.
     * @return this (for chaining calls)
     */
    T start();

    /**
     * Get info if service is running or not.
     * @return
     */
    boolean isRunning();

    /**
     * Initiates service stop and waits until service has stopped
     * or timeout occurs.
     */
    void stop(long timeout);

    /**
     * Get id.
     * @return id
     */
    String getId();

}