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
import net.jexler.core.JexlerSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jexler system handler.
 * 
 * @author $(whois jexler.net)
 */
public class JexlerSystemHandler extends AbstractJexlerHandler {

    public static enum Method {
        SHUTDOWN;
    }

    static final Logger log = LoggerFactory.getLogger(JexlerSystemHandler.class);

    private JexlerSystem system;
    
    /**
     * Constructor from id and system.
     * @param id id
     * @param system system
     */
    public JexlerSystemHandler(String id, JexlerSystem system) {
            super(id);
            this.system = system;
    }
    
    @Override
    public boolean canHandle(JexlerMessage message) {
        return this.getClass() == message.get("destination.class")
            && this.getId().equals(message.get("destination.id"));
    }

    @Override
    public void handle(JexlerMessage message) {
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
                case SHUTDOWN: system.shutdown();
            }
        }
    }

}
