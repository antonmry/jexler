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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service which is a group of services.
 * Starting starts all, stopping stops all.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class ServiceGroup implements Service {

    private static final Logger log = LoggerFactory.getLogger(ServiceGroup.class)

    private final String id
    private final List<Service> services

    /**
     * Constructor.
     * @param id the service group id
     */
    ServiceGroup(String id) {
        this.id = id
        this.services = new LinkedList<>()
    }
    
    /**
     * Start all services in the group.
     * Runtime exceptions are not caught, hence if the
     * first service throws while starting up, no attempt
     * is made to start the others.
     */
    @Override
    void start() {
        synchronized(services) {
            for (Service service : services) {
                service.start()
            }
        }
    }

    @Override
    boolean waitForStartup(long timeout) {
        return ServiceUtil.waitForStartup(this, timeout)
    }

    /**
     * Stop all services in a group.
     * Runtime exceptions are only logged, hence it is always
     * attempted to stop all services.
     */
    @Override
    void stop() {
        RuntimeException ex = null
        synchronized(services) {
            for (Service service : services) {
                try {
                    service.stop()
                } catch (RuntimeException e) {
                    if (ex == null) {
                        ex = e
                    }
                    log.trace("Could not stop service '$id'", e)
                }
            }
        }
        if (ex != null) {
            throw ex
        }
    }

    @Override
    boolean waitForShutdown(long timeout) {
        return ServiceUtil.waitForShutdown(this, timeout)
    }

    /**
     * Get service state of the group.
     * @return If there is no service in the group, OFF is returned,
     *   if all services are in the same state, that state is returned,
     *   if a least one service is starting up, BUSY_STARTING is returned,
     *   else IDLE is returned.
     */
    @Override
    ServiceState getState() {
        Set<ServiceState> set = new HashSet<>()
        synchronized(services) {
            for (Service service : services) {
                set.add(service.state)
            }
        }
        switch (set.size()) {
        case 0: return ServiceState.OFF
        case 1: return set.iterator().next()
        default:
            if (set.contains(ServiceState.BUSY_STARTING)) {
                return ServiceState.BUSY_STARTING
            } else {
                return ServiceState.IDLE
            }
        }
    }

    /**
     * Check if service is on.
     * Considered on if at least one service in the group is on.
     */
    @Override
    boolean isOn() {
        synchronized(services) {
            for (Service service : services) {
                if (service.on) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if service is off.
     * Considered only off if all services in the group are off.
     */
    @Override
    boolean isOff() {
        return !on
    }

    @Override
    void zap() {
        synchronized(services) {
            for (Service service : services) {
                service.zap()
            }
        }
    }

    @Override
    String getId() {
        return id
    }

    /**
     * Add given service to the group of services.
     */
    void add(Service service) {
        synchronized(services) {
            services.add(service)
        }
    }

    /**
     * Get the list of services.
     * Use also to modify the group of services.
     * @return list of services, never null
     */
    List<Service> getServices() {
        synchronized(services) {
            return services
        }
    }

    
}
