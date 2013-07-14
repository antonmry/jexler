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

package net.jexler.internal;

import java.util.Date;

import net.jexler.Issue;
import net.jexler.service.Service;

/**
 * Basic default implementation of issue interface.
 *
 * @author $(whois jexler.net)
 */
public class BasicIssue implements Issue {
	
    private final Date date;
    private final Service service;
    private final String message;
    private final Exception exception;
    private final String stackTrace;

    public BasicIssue(Service service, String message, Exception exception) {
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
        return -date.compareTo(issue.getDate());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue: [message=");
        if (message == null) {
        	builder.append(message);
        } else {
        	builder.append("'" + toSingleLine(message) + "'");
        }
        builder.append(",service=");
        if (service == null) {
        	builder.append(service);
        } else {
        	builder.append("'" + service.getClass().getName() + ":" + service.getId() + "'");
        }
        builder.append(",exception=");
        if (exception == null) {
        	builder.append(exception);
        } else {
        	builder.append("'" + toSingleLine(exception.toString()) + "'");
        }
        builder.append(",stackTrace=");
        if (stackTrace == null) {
        	builder.append(stackTrace);
        } else {
        	builder.append("'" + toSingleLine(stackTrace.toString()) + "'");
        }
        builder.append(']');
        return builder.toString();
    }
    
    private static String toSingleLine(String in) {
    	return in.replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n");
    }
}

