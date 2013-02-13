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

/**
 * Formal sensor for stopping a jexler, triggered externally.
 *
 * @author $(whois jexler.net)
 */
public class StopSensor extends AbstractSensor {

    public static class Event extends AbstractEvent {
        public Event(Sensor sensor) {
            super(sensor);
        }
    }

    /**
     * Constructor.
     */
    public StopSensor(EventHandler eventHandler, String id) {
        super(eventHandler, id);
    }

    public Sensor start() {
        return this;
    }

    public void stop() {
    }

    /**
     * Trigger event.
     */
    public void trigger() {
        eventHandler.handle(new Event(this));
    }

}
