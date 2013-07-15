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

import it.sauronsoftware.cron4j.Scheduler;
import net.jexler.Jexler;
import net.jexler.RunState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cron service, creates events at configurable times.
 * Implemented using the cron4j library.
 *
 * @author $(whois jexler.net)
 */
public class CronService extends ServiceBase {

    static final Logger log = LoggerFactory.getLogger(CronService.class);

    private final CronService thisService;
    private String cron;
    private Scheduler scheduler;

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    public CronService(Jexler jexler, String id) {
        super(jexler, id);
        thisService = this;
    }

    /**
     * Set cron pattern, e.g. "* * * * *".
     * @return this (for chaining calls)
     */
    public CronService setCron(String cron) {
        this.cron = cron;
        return this;
    }

    @Override
    public void start() {
        if (!isOff()) {
            return;
        }
        scheduler = new Scheduler();
        scheduler.start();
        Thread cronThread = new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName(getJexler().getId() + "|" + getId());
                log.trace("new cron event: " + cron);
                getJexler().handle(new CronEvent(thisService, cron));
            }
        });
        cronThread.setDaemon(true);
        setRunState(RunState.IDLE);
        scheduler.schedule(cron, cronThread);
    }

    @Override
    public void stop() {
        if (isOff()) {
            return;
        }
        scheduler.stop();
        setRunState(RunState.OFF);
    }

}
