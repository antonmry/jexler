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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jexler, runs a script that handles events.
 *
 * @author $(whois jexler.net)
 */
public class Jexler implements EventHandler {

    static final Logger log = LoggerFactory.getLogger(Jexler.class);

    private File file;
    private String name;
    private volatile boolean isRunning;
    private Thread scriptThread;
    private BlockingQueue<Event> events;

    /**
     * List of sensors.
     * Scripts are free to add sensors to this list or not - if they do,
     * sensors are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private List<Sensor> sensors;

    private StopSensor stopSensor;

    /**
     * Constructor.
     * @param file file with jexler script
     */
    public Jexler(File file) {
        this.file = file;
        name = file.getName();
        isRunning = false;
        events = new LinkedBlockingQueue<Event>();
        sensors = new LinkedList<Sensor>();
        stopSensor = new StopSensor(this, "stop-jexler");
    }

    public void start() {
        if (isRunning) {
            return;
        }

        events.clear();
        sensors.clear();
        sensors.add(stopSensor);

        final Map<String,Object> variables = new HashMap<>();
        variables.put("jexler", this);
        variables.put("file", file);
        variables.put("name", name);
        variables.put("events", events);
        variables.put("sensors", sensors);
        scriptThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            ScriptUtil.runScriptThreadSafe(variables, file);
                            // LATER handle returned object?
                        } catch (RuntimeException | ScriptException e) {
                            log.error("Jexler failed to run script {}: {}", name, e);
                            // HACK for the moment
                            System.out.println("--- Exception in Jexler Script ---");
                            e.printStackTrace();
                        } finally {
                            for (Sensor sensor : sensors) {
                                try {
                                    sensor.stop();
                                } catch (RuntimeException e) {
                                    log.error("Could not stop sensor {} {}: {}",
                                            sensor.getClass(), sensor.getId(), e);
                                }
                            }
                            isRunning = false;
                        }
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
        stopSensor.trigger();

        // LATER make sure that really stopped
        isRunning = false;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

}
