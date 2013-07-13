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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.jexler.RunState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic default implementation of service group interface.
 *
 * @author $(whois jexler.net)
 */
public class BasicServiceGroup implements ServiceGroup {
	
    static final Logger log = LoggerFactory.getLogger(BasicServiceGroup.class);

    private final String id;
	private final List<Service> services;

    /**
     * Constructor.
     */
    public BasicServiceGroup(String id) {
    	this.id = id;
    	this.services = new LinkedList<Service>();
    }
    
    /**
     * Start all services in the group.
     * Runtime exceptions are not caught, i.e. if the
     * first service throws while starting up, no attempt
     * is made to start the others.
     */
	@Override
    public void start() {
        for (Service service : services) {
        	service.start();
        }
    }

	@Override
	public boolean waitForStartup(long timeout) {
		return ServiceUtil.waitForStartup(this, timeout);
	}
	
	@Override
    public void stop() {
		RuntimeException ex = null;
        for (Service service : services) {
        	try {
    			service.stop();
    		} catch (RuntimeException e) {
    			if (ex == null) {
    				ex = e;
    			}
    			log.trace("Could not stop service '" + id + "'", e);
    		}
        }
        if (ex != null) {
        	throw ex;
        }
    }

	@Override
	public boolean waitForShutdown(long timeout) {
		return ServiceUtil.waitForShutdown(this, timeout);
	}

    /**
     * Get run state for the group.
     * If there is no service in the group, OFF is returned;
     * if all services are in the same state, that state is returned;
     * otherwise IDLE is returned, since at least one service must
     * then have been not OFF.
     */
    @Override
    public RunState getRunState() {
    	Set<RunState> set = new HashSet<RunState>();
    	for (Service service : services) {
        	set.add(service.getRunState());
        }
    	switch (set.size()) {
    	case 0: return RunState.OFF;
    	case 1: return set.iterator().next();
    	default: return RunState.IDLE;
    	}
    }

	/**
	 * Check if service is on.
	 * Considered on if at least one service in the group is on.
	 */
	@Override
    public boolean isOn() {
        for (Service service : services) {
            if (service.isOn()) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Check if service is off.
	 * Considered only off if all services in the group are off.
	 */
	@Override
    public boolean isOff() {
		return !isOn();
    }

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void add(Service service) {
		services.add(service);
	}

	@Override
	public List<Service> getServiceList() {
		return services;
	}

    
}
