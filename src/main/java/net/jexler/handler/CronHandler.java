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

package net.jexler.handler;

import it.sauronsoftware.cron4j.Scheduler;
import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerSubmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Cron handler.
 * 
 * @author $(whois jexler.net)
 */
public class CronHandler extends AbstractJexlerHandler {

    static final Logger log = LoggerFactory.getLogger(CronHandler.class);
        
    private class CronThread extends Thread {
            public void run() {
                submitter.submit(cronMessage);
            }
    }
    
    private final String cron;
    private final JexlerMessage cronMessage;
    private final Scheduler scheduler;

    /**
     * Constructor.
     * @param id id
     * @param cron cron string, e.g. "* * * * *"
     * @param cronId id of message to send when time has come
     */
    public CronHandler(String id, String cron, String cronId) {
            super(id);
            this.cron = cron;
            cronMessage = new JexlerMessage(
                    "sender", this,
                    "cronid", cronId,
                    "info", "cron-job:" + cronId);
            scheduler = new Scheduler();
    }
    
    @Override
    public void startup(JexlerSubmitter submitter) {
        super.startup(submitter);
        scheduler.start();
        scheduler.schedule(cron, new CronThread());
    }
    
    @Override
    public void shutdown() {
        scheduler.stop();
    }
    
}
