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
public class Jexler implements Service<Jexler>, EventHandler {

    static final Logger log = LoggerFactory.getLogger(Jexler.class);

    private File file;
    private String id;

    /**
     * Set to true just at the beginning of start() and set to false
     * once the script exits in any way, after it has been tried to
     * stop all sensors and actors.
     */
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

    /**
     * List of actors.
     * Scripts are free to add actors to this list or not - if they do,
     * actors are automatically stopped by jexler after the script exits
     * (regularly or throws).
     */
    private List<Actor> actors;

    private StopSensor stopSensor;

    /**
     * Constructor.
     * @param file file with jexler script
     */
    public Jexler(File file) {
        this.file = file;
        id = file.getName();
        isRunning = false;
        events = new LinkedBlockingQueue<Event>();
        sensors = new LinkedList<Sensor>();
        actors = new LinkedList<Actor>();
        stopSensor = new StopSensor(this, "stop-jexler");
    }

    /**
     * Immediately sets isRunning to true, then tries to start the script.
     * Typically returns before the jexler script has started or completed
     * to initialize all its services.
     * The jexler remains in the running state until the script exits in
     * any way, after it has been tried to stop all registered services
     * (sensors and actors).
     *
     * LATER stash() method to put hanging jexlers away? and maybe try to
     * stop all services even when the script is still running?
     */
    @Override
    public Jexler start() {
        if (isRunning) {
            return this;
        }

        isRunning = true;

        sensors.add(stopSensor);

        final Map<String,Object> variables = new HashMap<>();
        variables.put("jexler", this);
        variables.put("file", file);
        variables.put("id", id);
        variables.put("events", events);
        variables.put("sensors", sensors);
        variables.put("actors", actors);
        scriptThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            ScriptUtil.runScriptThreadSafe(variables, file);
                            // LATER log returned object?
                        } catch (RuntimeException | ScriptException e) {
                            log.error("Jexler failed to run script {}: {}", id, e);
                            // HACK for the moment
                            System.out.println("--- Exception in Jexler Script ---");
                            e.printStackTrace();
                        } finally {
                            for (Sensor sensor : sensors) {
                                try {
                                    sensor.stop(0);
                                } catch (RuntimeException e) {
                                    log.error("Could not stop sensor {} {}: {}",
                                            sensor.getClass(), sensor.getId(), e);
                                }
                            }
                            for (Actor actor : actors) {
                                try {
                                    actor.stop(0);
                                } catch (RuntimeException e) {
                                    log.error("Could not stop actor {} {}: {}",
                                            actor.getClass(), actor.getId(), e);
                                }
                            }
                            events.clear();
                            sensors.clear();
                            actors.clear();
                            isRunning = false;
                        }
                    }
                });
        scriptThread.setDaemon(true);
        scriptThread.start();

        return this;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void handle(Event event) {
        events.add(event);
    }

    /**
     * Sends stop event to jexler and waits until stopped or timeout.
     */
    @Override
    public void stop(long timeout) {
        if (!isRunning) {
            return;
        }

        stopSensor.trigger();

        long t0 = System.currentTimeMillis();
        while (isRunning && System.currentTimeMillis() - t0 < timeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Get id (script file name with extension).
     */
    public String getId() {
        return id;
    }

    /**
     * Get script file.
      */
    public File getFile() {
        return file;
    }

    /**
     * Get id for given file (file name).
     * @param file file
     * @return id
     */
    public static String getIdForFile(File file) {
        return file.getName();
    }

}
