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

import java.util.Date;

import net.jexler.service.Service;

/**
 * Interface for an issue.
 * Issues are typically created and attached to a jexler
 * or jexler container if something could not be done,
 * often because some exception occurred.
 *
 * @author $(whois jexler.net)
 */
public interface Issue extends Comparable<Issue> {

	/**
	 * Get date and time of when the issue occurred.
	 */
    Date getDate();

    /**
     * Get service where the issue occurred, may be null.
     */
    Service getService();

    /**
     * Get message set when issue was created.
     */
    String getMessage();

    /**
     * Get throwable that caused the issue, or null if none.
     */
    Throwable getCause();

    /**
     * Get exception stack trace as a multi-line string.
     * @return stack trace or empty if none or could not obtain it
     */
    String getStackTrace();

    /**
     * Compares issues, newer is smaller (first in a sorted list).
     */
    @Override
    int compareTo(Issue issue);

}

