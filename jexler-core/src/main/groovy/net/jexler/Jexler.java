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

import java.io.File;

import net.jexler.service.Event;
import net.jexler.service.Service;

/**
 * Interface for a jexler, runs a Groovy script that handles events.
 *
 * @author $(whois jexler.net)
 */
public interface Jexler extends Service, IssueTracker {

    /**
     * Handle given event.
     */
    void handle(Event event);
    
    /**
     * Get script file.
     */
    File getFile();
    
    /**
     * Get directory that contains script file.
     */
    File getDir();
    
    /**
     * Get meta info.
     * Read from the jexler file at each call except if the jexler
     * is already running, in that case returns meta info read at
     * the time the jexler was started.
     */
    MetaInfo getMetaInfo();

    /**
     * Get the container that contains this jexler.
     */
    JexlerContainer getContainer();

}
