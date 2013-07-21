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

import net.jexler.Jexler;
import net.jexler.RunState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "once" service, creates a single event at start.
 *
 * @author $(whois jexler.net)
 */
public class OnceService extends ServiceBase {

    static final Logger log = LoggerFactory.getLogger(OnceService.class);

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    public OnceService(Jexler jexler, String id) {
        super(jexler, id);
    }

    @Override
    public void start() {
        if (!isOff()) {
            return;
        }
        log.trace("new once event");
        getJexler().handle(new OnceEvent(this));
        setRunState(RunState.IDLE);
    }

    @Override
    public void stop() {
        if (isOff()) {
            return;
        }
        setRunState(RunState.OFF);
    }

}
