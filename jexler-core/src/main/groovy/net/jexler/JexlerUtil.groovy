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

package net.jexler

import net.jexler.service.ServiceState
import net.jexler.service.ServiceUtil

import groovy.transform.CompileStatic

/**
 * Jexler utilities.
 * Includes some static methods that might be useful in Groovy scripts
 * or in Java (for writing custom services or tools).
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerUtil {

    private static final String STARTUP_TIMEOUT_MSG = 'Timeout waiting for jexler startup.'
    private static final String SHUTDOWN_TIMEOUT_MSG = 'Timeout waiting for jexler shutdown.'

    /**
     * Wait for jexler startup and report issue if did not start in time.
     * @return true if started within timeout
     */
    static boolean waitForStartup(Jexler jexler, long timeout) {
        if (ServiceUtil.waitForStartup(jexler, timeout)) {
            return true
        }
        jexler.trackIssue(jexler, STARTUP_TIMEOUT_MSG, null)
        return false
    }


    /**
     * Wait for jexler shutdown and report issue if did not shut down in time.
     * @return true if shut down within timeout
     */
    static boolean waitForShutdown(Jexler jexler, long timeout) {
        if (ServiceUtil.waitForShutdown(jexler, timeout)) {
            return true
        }
        jexler.trackIssue(jexler, SHUTDOWN_TIMEOUT_MSG, null)
        return false
    }

    /**
     * Wait for container startup and report an issue for each
     * jexler that did not start in time.
     * @return true if all jexlers started within timeout
     */
    static boolean waitForStartup(JexlerContainer container, long timeout) {
        if (ServiceUtil.waitForStartup(container, timeout)) {
            return true
        }
        for (Jexler jexler : container.jexlers) {
            if (jexler.state == ServiceState.BUSY_STARTING) {
                container.trackIssue(jexler, STARTUP_TIMEOUT_MSG, null)
            }
        }
        return false
    }

    /**
     * Wait for container shutdown and report an issue for each
     * jexler that did not shut down in time.
     * @return true if all jexlers shut down within timeout
     */
    static boolean waitForShutdown(JexlerContainer container, long timeout) {
        if (ServiceUtil.waitForShutdown(container, timeout)) {
            return true
        }
        for (Jexler jexler : container.jexlers) {
            if (jexler.state != ServiceState.OFF) {
                container.trackIssue(jexler, SHUTDOWN_TIMEOUT_MSG, null)
            }
        }
        return false
    }

    /**
     * Get stack trace for given throwable as a string.
     * @return stack trace, never null, empty if throwable is null or could not obtain
     */
    static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return ''
        }
        try {
            final Writer result = new StringWriter()
            throwable.printStackTrace(new PrintWriter(result))
            return result
        } catch (RuntimeException ignore) {
            return ''
        }
    }
    
    /**
     * Replace line breaks in string with '%n'.
     * Replaces CRLF, CR, LF with '%n', in that order.
     * return string with replacements, null if given string is null
     */
    static String toSingleLine(String multi) {
        return multi?.replace('\r\n', '%n')?.replace('\r', '%n')?.replace('\n', '%n')
    }

    /**
     * Wait at least for the indicated time in milliseconds.
     * @param ms time to wait in ms
     */
    static void waitAtLeast(long ms) {
        final long t0 = System.currentTimeMillis()
        while (true) {
            final long t1 = System.currentTimeMillis()
            if (t1-t0 >= ms) {
                return
            }
            try {
                Thread.sleep(ms - (t1-t0))
            } catch (InterruptedException ignored) {
            }
        }
    }

}
