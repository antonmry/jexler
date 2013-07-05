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
import java.util.List;

import net.jexler.service.Service;

/**
 * All jexlers in a directory.
 *
 * @author $(whois jexler.net)
 */
public interface Jexlers extends Service, IssueTracker {

    /**
     * Refresh list of jexlers.
     */
    void refresh();

    /**
     * Start jexlers that are marked as autostart.
     */
    void autostart();

    File getDir();

    /**
     * Get jexlers, sorted by id.
     * @return jexlers
     */
    List<Jexler> getJexlers();

    /**
     * Get jexler for given id.
     * @param id
     * @return jexler for given id or null if none
     */
    Jexler getJexler(String id);

}
