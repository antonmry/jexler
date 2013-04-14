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

import net.jexler.AbstractEvent;
import net.jexler.AbstractService;
import net.jexler.Jexler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * A cron service, creates events at configurable times.
 *
 * @author $(whois jexler.net)
 */
public class CronService extends AbstractService {

    public static class Event extends AbstractEvent {
        private String cron;
        public Event(CronService service, String cron) {
            super(service);
            this.cron = cron;
        }
        public String getCron() {
            return cron;
        }
    }

    static final Logger log = LoggerFactory.getLogger(CronService.class);

    private final CronService thisService;
    private String cron;
    private Scheduler scheduler;

    /**
     * Constructor.
     */
    public CronService(Jexler jexler, String id) {
        super(jexler, id);
        thisService = this;
    }

    public CronService setCron(String cron) {
        this.cron = cron;
        return this;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        scheduler = new Scheduler();
        scheduler.start();
        Thread cronThread = new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName(getJexler().getId() + "|" + getId());
                log.trace("new cron event: " + cron);
                getJexler().handle(new Event(thisService, cron));
            }
        });
        cronThread.setDaemon(true);
        scheduler.schedule(cron, cronThread);
        setRunning(true);
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }
        scheduler.stop();
        setRunning(false);
    }

}
