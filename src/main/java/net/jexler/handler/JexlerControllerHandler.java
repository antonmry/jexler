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

import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.Jexler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jexler controller handler.
 *
 * @author $(whois jexler.net)
 */
public class JexlerControllerHandler extends AbstractJexlerHandler {

    public static enum Method {
        SHUTDOWN;
    }

    static final Logger log = LoggerFactory.getLogger(JexlerControllerHandler.class);

    private Jexler jexler;

    /**
     * Constructor from id and jexler.
     * @param id id
     * @param jexler jexler
     */
    public JexlerControllerHandler(String id, String description, Jexler jexler) {
        super(id, description);
        this.jexler = jexler;
    }

    @Override
    public boolean canHandle(JexlerMessage message) {
        return this.getClass() == message.get("destination.class")
            && this.getId().equals(message.get("destination.id"));
    }

    @Override
    public boolean handle(JexlerMessage message) {
        Object methodObj = message.get("destination.method");
        Method method = null;
        try {
            method = (Method)methodObj;
        } catch (ClassCastException e) {
        }
        if (method == null) {
            log.error("Unknown method " + methodObj);
        } else {
            switch (method) {
                case SHUTDOWN: jexler.shutdown();
            }
        }
        return false;
    }

}
