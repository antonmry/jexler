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
import net.jexler.JexlerUtil;
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
    private final Throwable cause;
    private final String stackTrace;

    /**
     * Constructor from service, message and exception.
     */
    public BasicIssue(Service service, String message, Throwable cause) {
        date = new Date();
        this.service = service;
        this.message = message;
        this.cause = cause;
        stackTrace = JexlerUtil.getStackTrace(cause);
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public int compareTo(Issue issue) {
        // newest date first
        return -date.compareTo(issue.getDate());
    }

    /**
     * Create a single line string of all members, suitable for logging.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Issue: [message=");
        if (message == null) {
        	builder.append("null");
        } else {
        	builder.append("'" + JexlerUtil.toSingleLine(message) + "'");
        }
        builder.append(",service=");
        if (service == null) {
        	builder.append("null");
        } else {
        	builder.append("'" + service.getClass().getName() + ":" + service.getId() + "'");
        }
        builder.append(",cause=");
        if (cause == null) {
        	builder.append("null");
        } else {
        	builder.append("'" + JexlerUtil.toSingleLine(cause.toString()) + "'");
        }
        builder.append(",stackTrace=");
        builder.append("'" + JexlerUtil.toSingleLine(stackTrace) + "'");
        builder.append(']');
        return builder.toString();
    }
}

