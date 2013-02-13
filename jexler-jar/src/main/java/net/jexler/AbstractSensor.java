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


/**
 * Abstract base sensor.
 *
 * @author $(whois jexler.net)
 */
public abstract class AbstractSensor implements Sensor {

    protected EventHandler eventHandler;
    protected String id;
    protected volatile boolean isRunning;

    /**
     * Constructor.
     */
    public AbstractSensor(EventHandler eventHandler, String id) {
        this.eventHandler = eventHandler;
        this.id = id;
        isRunning = false;
    }

    @Override
    public String getId() {
        return id;
    }
}
