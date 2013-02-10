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

package net.jexler.sensor;

import it.sauronsoftware.cron4j.Scheduler;
import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerMessageFactory;
import net.jexler.core.JexlerSubmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Cron handler.
 *
 * @author $(whois jexler.net)
 */
public class CronSensor extends AbstractJexlerHandler {

    static final Logger log = LoggerFactory.getLogger(CronSensor.class);

    private class CronThread extends Thread {
        public void run() {
            submitter.submit(cronMessage);
        }
    }

    private String cron;
    private JexlerMessage cronMessage;
    private Scheduler scheduler;

    /**
     * Constructor.
     * @param id id
     * @param description description
     */
    public CronSensor(String id, String description) {
        super(id, description);
    }

    /**
     * Set cron string, e.g. "* * * * *"
     * @param cron
     */
    public void setCron(String cron) {
        this.cron = cron;
    }

    @Override
    public void start(JexlerSubmitter submitter) {
        super.start(submitter);
        cronMessage = JexlerMessageFactory.create().set(
                "sender", this,
                "id", getId(),
                "info", "cron-" + getId());
        scheduler = new Scheduler();
        scheduler.start();
        CronThread cronThread = new CronThread();
        cronThread.setDaemon(true);
        scheduler.schedule(cron, cronThread);
    }

    @Override
    public void stop() {
        scheduler.stop();
    }

}
