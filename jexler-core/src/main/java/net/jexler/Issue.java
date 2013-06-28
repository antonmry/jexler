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

import net.jexler.impl.JexlerUtil;

/**
 * Issue.
 *
 * @author $(whois jexler.net)
 */
public class Issue implements Comparable<Issue> {

    private final Date date;
    private final Service service;
    private final String message;
    private final Exception exception;
    private final String stackTrace;

    public Issue(Service service, String message, Exception exception) {
        date = new Date();
        this.service = service;
        this.message = message;
        this.exception = exception;
        if (exception != null) {
            stackTrace = JexlerUtil.getStackTrace(exception);
        } else {
        	stackTrace = null;
        }
    }

    public Date getDate() {
        return date;
    }

    public Service getService() {
        return service;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Get stack trace as a multi-line string.
     * @return stack trace or null if none
     */
    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public int compareTo(Issue issue) {
        // newest date first
        return -date.compareTo(issue.date);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue: [message='");
        builder.append(message);
        builder.append("',service='");
        builder.append(service != null ? service.getClass().getName() + ":" + service.getId() : null);
        builder.append("',exception='");
        builder.append(exception != null ? exception.toString().replace("\n", "\\n") : null);
        builder.append("',stackTrace='");
        builder.append(stackTrace != null ? stackTrace.replace("\n", "\\n") : null);
        builder.append(']');
        return builder.toString();
    }
}

