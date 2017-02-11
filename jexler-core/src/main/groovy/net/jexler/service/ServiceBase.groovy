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

package net.jexler.service

import groovy.transform.CompileStatic

/**
 * Abstract service base implementation.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
abstract class ServiceBase implements Service {

    private final String id
    private volatile ServiceState state

    /**
     * Constructor.
     * @param jexler the jexler to send events to
     * @param id the id of the service
     */
    ServiceBase(String id) {
        this.id = id
        state = ServiceState.OFF
    }

    /**
     * Set state to given value.
     */
    void setState(ServiceState state) {
        this.state = state
    }
        
    @Override
    boolean waitForStartup(long timeout) {
           return ServiceUtil.waitForStartup(this, timeout)
    }
    
    @Override
    boolean waitForShutdown(long timeout) {
           return ServiceUtil.waitForShutdown(this, timeout)
    }
    
    @Override
    ServiceState getState() {
        return state
    }

    @Override
    String getId() {
        return id
    }

    @Override
    void zap() {
        state = ServiceState.OFF
    }

}
