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

import java.nio.file.Path;
import java.nio.file.WatchEvent;



/**
 * Directory watch service event.
 *
 * @author $(whois jexler.net)
 */
public class DirWatchEvent extends EventBase {

    private Path filePath;
    private WatchEvent.Kind<?> kind;
    
    public DirWatchEvent(Service service, Path filePath, WatchEvent.Kind<?> kind) {
        super(service);
        this.filePath = filePath;
        this.kind = kind;
    }
    
    /**
     * Path to file that been created, modified or deleted.
     * @return path
     */
    public Path getFilePath() {
        return filePath;
    }
    
    public WatchEvent.Kind<?> getKind() {
        return kind;
    }

}
