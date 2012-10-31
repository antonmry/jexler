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
public interface JexlerHandler extends JexlerInfo {

    /**
     * Startup handler.
     * Writes possibly messages to submitter,
     * even after call returned.
     * @param submitter
     */
    void startup(JexlerSubmitter submitter);

    /**
     * Say if can handle message or not (does nothing more).
     * @param message message
     * @return true if can handle
     */
    boolean canHandle(JexlerMessage message);

    /**
     * Handle message.
     * Called by jexler system in separate thread from canHandle()
     * if the latter returned true.
     * Writes possibly messages to submitter given in startup().
     * @param message message
     * @return true if handled for good, false if should be passed
     *         on to remaining handlers (try canHandle() first).
     */
    boolean handle(JexlerMessage message);

    /**
     * Shutdown handler.
     */
    void shutdown();

}
