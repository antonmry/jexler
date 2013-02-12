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

import it.sauronsoftware.cron4j.Scheduler;




/**
 * A cron sensor.
 *
 * @author $(whois jexler.net)
 */
public class CronSensor implements Sensor {

    private class CronThread extends Thread {
        public void run() {
            eventHandler.handle(new CronEvent(cron));
        }
    }

    private Scheduler scheduler;
    private EventHandler eventHandler;
    private String cron;

    /**
     * Constructor.
     */
    public CronSensor(EventHandler eventHandler, String cron) {
        this.eventHandler = eventHandler;
        this.cron = cron;
    }

    public void start() {
        scheduler = new Scheduler();
        scheduler.start();
        CronThread cronThread = new CronThread();
        cronThread.setDaemon(true);
        scheduler.schedule(cron, cronThread);
    }

    public void stop() {
        scheduler.stop();
    }

}
