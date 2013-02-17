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
 * Abstract base service.
 *
 * @author $(whois jexler.net)
 */
public abstract class AbstractService<T> implements Service<T> {

    private EventHandler<T> eventHandler;
    private String id;
    private volatile boolean isRunning;

    /**
     * Constructor.
     */
    public AbstractService(EventHandler<T> eventHandler, String id) {
        this.eventHandler = eventHandler;
        this.id = id;
        isRunning = false;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getId() {
        return id;
    }

    protected EventHandler<T> getEventHandler() {
        return eventHandler;
    }

}
