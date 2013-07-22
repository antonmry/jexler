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

/**
 * Service utilities.
 * Includes some static methods that might be useful in Groovy scripts
 * or in Java (for writing custom services).
 *
 * @author $(whois jexler.net)
 */
public class ServiceUtil {
	
	@SuppressWarnings("serial")
	static class NoInstanceException extends Exception {
	}
	
	/**
	 * Don't use, class contains only static utility methods.
	 * @throws NoInstanceException Always.
	 */
	public ServiceUtil() throws NoInstanceException {
		throw new NoInstanceException();
	}
		
    public static boolean waitForStartup(Service service, long timeout) {
    	long t0 = System.currentTimeMillis();
    	do {
    		if (!service.getRunState().isBusyStarting()) {
    			return true;
    		}
    		if (System.currentTimeMillis() - t0 >= timeout) {
    			return false;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
    }
    
    public static boolean waitForShutdown(Service service, long timeout) {
       	long t0 = System.currentTimeMillis();
    	do {
    		if (service.isOff()) {
    			return true;
    		}
    		if (System.currentTimeMillis() - t0 >= timeout) {
    			return false;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
    }

}
