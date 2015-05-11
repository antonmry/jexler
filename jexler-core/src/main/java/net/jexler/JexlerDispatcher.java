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

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.Script;
import net.jexler.internal.BasicJexler.Events;
import net.jexler.service.Event;
import net.jexler.service.StopEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Jexler dispatcher.
 *
 * Allows to handle lifecycle and events a bit more structured
 * by implementing specific methods like start() or handleCronEvent()
 * in the jexler script.
 *
 * @author $(whois jexler.net)
 */
public class JexlerDispatcher {

    private static final Logger log = LoggerFactory.getLogger(JexlerDispatcher.class);

    @SuppressWarnings("serial")
    static class NoInstanceException extends Exception {
    }

    /**
     * Don't use, class contains only static utility methods.
     * @throws NoInstanceException Always.
     */
    public JexlerDispatcher() throws NoInstanceException {
        throw new NoInstanceException();
    }

    public static void dispatch(Script script) {

        Jexler jexler = (Jexler)script.getBinding().getVariable("jexler");
        Events events = (Events)script.getBinding().getVariable("events");

        MetaClass mc = script.getMetaClass();
        Object[] noArgs = {};

        MetaMethod mm = mc.getMetaMethod("declare", noArgs);
        if (mm != null) {
            mm.invoke(script, noArgs);
        }

        mm = mc.getMetaMethod("start", noArgs);
        if (mm == null) {
            jexler.trackIssue(jexler, "Dispatch: Mandatory start() method missing.", null);
            return;
        } else {
            mm.invoke(script, noArgs);
        }

        while(true) {
            Event event = events.take();

            if (event instanceof StopEvent) {
                mm = mc.getMetaMethod("stop", noArgs);
                if (mm != null) {
                    mm.invoke(script, noArgs);
                }
                return;
            }

            String eventClassName = event.getClass().getSimpleName();
            String eventServiceId = event.getService().getId();
            Object[] eventArgs = { Event.class };

            mm = mc.getMetaMethod("handle" + eventClassName + eventServiceId, eventArgs);
            if (mm == null) {
                mm = mc.getMetaMethod("handle" + eventClassName, eventArgs);
                if (mm == null) {
                    mm = mc.getMetaMethod("handle", eventArgs);
                }
            }
            if (mm == null) {
                jexler.trackIssue(jexler, "Dispatch: No handler for event " + eventClassName
                        + " from service " + eventServiceId + ".", null);
            } else {
                try {
                    mm.invoke(script, new Object[]{event});
                } catch (Throwable t) {
                    jexler.trackIssue(jexler, "Dispatch: Handler " + mm.getName() + " failed.", t);
                }
            }
        }
    }

}
