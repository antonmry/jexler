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

import it.sauronsoftware.cron4j.Scheduler;

/**
 * A cron sensor, creates events at configurable times.
 *
 * @author $(whois jexler.net)
 */
public class CronSensor extends AbstractSensor {

    public static class Event extends AbstractEvent {
        private String cron;
        public Event(Sensor sensor, String cron) {
            super(sensor);
            this.cron = cron;
        }
        public String getCron() {
            return cron;
        }
    }

    private CronSensor thisSensor;
    private String cron;
    private Scheduler scheduler;

    /**
     * Constructor.
     */
    public CronSensor(EventHandler eventHandler, String id) {
        super(eventHandler, id);
        thisSensor = this;
    }

    public CronSensor setCron(String cron) {
        this.cron = cron;
        return this;
    }


    public Sensor start() {
        if (isRunning) {
            return this;
        }
        scheduler = new Scheduler();
        scheduler.start();
        Thread cronThread = new Thread(new Runnable() {
            public void run() {
                eventHandler.handle(new Event(thisSensor, cron));
            }
        });
        cronThread.setDaemon(true);
        scheduler.schedule(cron, cronThread);
        isRunning = true;
        return this;
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        scheduler.stop();
    }

}
