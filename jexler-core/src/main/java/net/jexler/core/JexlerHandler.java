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


/**
 * Interface for jexler handler.
 *
 * @author $(whois jexler.net)
 */
public interface JexlerHandler {

    /**
     * Start handler.
     * Writes possibly messages to submitter,
     * even after call returned.
     * @param submitter
     */
    void start(JexlerSubmitter submitter);

    /**
     * Handle message.
     * Writes possibly messages to submitter given in start().
     * @param message message
     * @return message to pass on or null
     */
    JexlerMessage handle(JexlerMessage message);

    /**
     * Stop handler and release all resources.
     */
    void stop();

    /**
     * Get id, unique per implementing class.
     * @return id, never null
     */
    String getId();

    /**
     * Get human readable description of handler.
     * @return description, never null
     */
    String getDescription();

}
