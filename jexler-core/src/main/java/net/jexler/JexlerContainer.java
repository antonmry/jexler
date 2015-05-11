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

import java.io.Closeable;
import java.io.File;
import java.util.List;

import it.sauronsoftware.cron4j.Scheduler;
import net.jexler.service.Service;

/**
 * Interface for the container of all jexlers in a directory.
 *
 * @author $(whois jexler.net)
 */
public interface JexlerContainer extends Service, IssueTracker, Closeable {

    /**
     * Refresh list of jexlers.
     */
    void refresh();

    /**
     * Start jexlers that are marked as autostart.
     */
    void autostart();

    /**
     * Get directory that contains the jexler Groovy scripts.
     */
    File getDir();

    /**
     * Get the list of all jexlers, sorted by id.
     *
     * This is a copy, iterating over it can be freely done;
     * and trying to add or remove list elements throws
     * an UnsupportedOperationException.
     */
    List<Jexler> getJexlers();

    /**
     * Get the jexler for the given id.
     * @return jexler for given id or null if none
     */
    Jexler getJexler(String id);
    
    /**
     * Get the file for the given jexler id,
     * even if no such file exists (yet).
     */
    File getJexlerFile(String id);
    
    /**
     * Get the jexler id for the given file,
     * even if the file does not exist (any more),
     * or null if not a jexler script.
     */
    String getJexlerId(File jexlerFile);

    /**
     * Get shared cron4j scheduler, already started.
     */
    Scheduler getSharedScheduler();

    /**
     * Stop the shared scheduler, plus close maybe other things.
     */
    @Override
    void close();

}
