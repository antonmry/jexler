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
 * Issue.
 *
 * @author $(whois jexler.net)
 */
public interface Issue extends Comparable<Issue> {

    public Date getDate();

    public Service getService();

    public String getMessage();

    public Exception getException();

    /**
     * Get stack trace as a multi-line string.
     * @return stack trace or empty if none or could not obtain it
     */
    public String getStackTrace();

    public int compareTo(Issue issue);

}

