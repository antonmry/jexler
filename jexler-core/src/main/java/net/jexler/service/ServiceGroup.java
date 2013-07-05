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

import java.util.List;


/**
 * Interface for a service which is a group of services.
 * Starting starts all, stopping stops all.
 *
 * @author $(whois jexler.net)
 */
public interface ServiceGroup extends Service {
	
	/**
	 * Add a service to the group of services.
	 * @param service
	 */
	void add(Service service);
	
	/**
	 * Get the list of services.
	 * Use to read and modify the group of services.
	 * @return list of services, never null
	 */
	List<Service> getServiceList();

}
