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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import net.jexler.Jexler;
import net.jexler.RunState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory watch service, creates an event when a file
 * in a given directory is created, modified oder deleted.
 *
 * @author $(whois jexler.net)
 */
public class DirWatchService extends ServiceBase {

    static final Logger log = LoggerFactory.getLogger(DirWatchService.class);

    private final DirWatchService thisService;
    private File watchDir;
    private long sleepTimeMs;

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    public DirWatchService(Jexler jexler, String id) {
        super(jexler, id);
        thisService = this;
        this.watchDir = jexler.getDir();
        this.sleepTimeMs = 1000;
    }

    /**
     * Set directory to watch.
     * Default if not set is the directory that contains the jexler.
     * @param watchDir directory to watch
     * @return this (for chaining calls)
     */
    public DirWatchService setDir(File watchDir) {
        this.watchDir = watchDir;
        return this;
    }

    /**
     * Set time to sleep between polling file system.
     * Default if not set is 1000ms (1 sec).
     * @param sleepTimeMs time to sleep in ms
     * @return this (for chaining calls)
     */
    public DirWatchService setSleepTimeMs(long sleepTimeMs) {
        this.sleepTimeMs = sleepTimeMs;
        return this;
    }

    @Override
    public void start() {
        if (!isOff()) {
            return;
        }
        Path path = watchDir.toPath();
        final WatchService watchService;
        final WatchKey watchKey;
        try {
        	watchService = path.getFileSystem().newWatchService();
        	watchKey = path.register(watchService,
        			StandardWatchEventKinds.ENTRY_CREATE,
        			StandardWatchEventKinds.ENTRY_MODIFY,
        			StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
        	getJexler().trackIssue(this, "could not create watch service or key for directory '"
        			+ watchDir.getAbsolutePath() + "'", e);
        	return;
        }
        Thread watchThread = new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName(getJexler().getId() + "|" + getId());
                while (getRunState() == RunState.IDLE) {
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                		Path path = ((Path)event.context());
                		File file = new File(watchDir, path.toFile().getName());
                		WatchEvent.Kind<?> kind = event.kind();
                		log.trace("event " + kind + " '" + file.getAbsolutePath() + "'");
                		getJexler().handle(new DirWatchEvent(thisService, file, kind));
                	}
                	if (getRunState() != RunState.IDLE) {
                		break;
                	}
                	try {
    					Thread.sleep(sleepTimeMs);
    				} catch (InterruptedException e) {
    				}
                }
                try {
					watchService.close();
				} catch (IOException e) {
					log.trace("failed to close watch service", e);
				}
                setRunState(RunState.OFF);
            }
        });
        watchThread.setDaemon(true);
        setRunState(RunState.IDLE);
        watchThread.start();
    }

    @Override
    public void stop() {
        if (isOff()) {
            return;
        }
        setRunState(RunState.BUSY_STOPPING);
    }

}
