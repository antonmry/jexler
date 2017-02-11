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

package net.jexler.service

import groovy.transform.CompileStatic

import java.nio.file.WatchEvent

/**
 * Directory watch service event.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class DirWatchEvent extends EventBase {

    /** File that has been created, modified or deleted. */
    final File file

    /** What happened with the file. */
    final WatchEvent.Kind kind
    
    /**
     * Constructor.
     * @param service the service that created the event
     * @param file the file that has been created, modified or deleted
     * @param kind what happened with the file
     */
    DirWatchEvent(Service service, File file, WatchEvent.Kind kind) {
        super(service)
        this.file = file
        this.kind = kind
    }

}
