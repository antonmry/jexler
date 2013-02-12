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

package net.jexler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



/**
 * A jexler.
 *
 * @author $(whois jexler.net)
 */
public class Jexler implements EventHandler {

    private File file;
    private volatile boolean isRunning;
    private Thread scriptThread;
    private BlockingQueue<Event> events;

    /**
     * Constructor.
     * LATER pass also suite? so can be used in scripts?
     * @param file file with jexler script
     */
    public Jexler(File file) {
        this.file = file;
        events = new LinkedBlockingQueue<Event>();
    }

    public void start() {
        if (isRunning) {
            return;
        }
        final Map<String,Object> variables = new HashMap<>();
        variables.put("jexler", this);
        variables.put("events", events);
        variables.put("file", file);
        scriptThread = new Thread(
                new Runnable() {
                    public void run() {
                        ScriptUtil.runScriptThreadSafe(variables, file);
                        // LATER handle returned object?
                    }
                });
        scriptThread.setDaemon(true);
        scriptThread.start();
        // LATER make sure that really started
        isRunning = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void handle(Event event) {
        events.add(event);
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        handle(new StopEvent());
        // LATER make sure that really stopped
        isRunning = false;
    }

    public String getName() {
        return file.getName();
    }

}
