/*
   Copyright 2012 $(whois jexler.net)

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

package net.jexler.core;

import java.util.List;


/**
 * Interface for a suite of jexlers.
 *
 * @author $(whois jexler.net)
 */
public interface JexlerSuite {

    /**
     * Get list of all jexlers, sorted alphabetically by id.
     * @return jexlers
     */
    List<Jexler> getJexlers();

    /**
     * Get jexler with given id.
     * @param id id
     * @return jexler or null
     */
    Jexler getJexler(String id);

    /**
     * Start up all jexlers.
     * LATER only ones with autostartup set
     */
    void startup();

    /**
     * Shutdown all jexlers.
     */
    void shutdown();

}
