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

package net.jexler.internal

import groovy.transform.CompileStatic
import net.jexler.Issue
import net.jexler.JexlerUtil
import net.jexler.service.Service

/**
 * Basic default implementation of issue interface.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class BasicIssue implements Issue {

    final Date date
    final Service service
    final String message
    final Throwable cause
    final String stackTrace

    /**
     * Constructor from service, message and exception.
     */
    BasicIssue(Service service, String message, Throwable cause) {
        date = new Date()
        this.service = service
        this.message = message
        this.cause = cause
        stackTrace = JexlerUtil.getStackTrace(cause)
    }

    @Override
    public int compareTo(Issue issue) {
        // newest date first
        return -date.compareTo(issue.date)
    }

    /**
     * Create a single line string of all members, suitable for logging.
     */
    @Override
    public String toString() {
        return """Issue: [message=${message==null ? 'null' : "'${JexlerUtil.toSingleLine(message)}'"}\
,service=${service == null ? 'null' : "'${service.class.name + ":" + service.id}'"}\
,cause=${cause == null ? 'null' : "'${JexlerUtil.toSingleLine(cause.toString())}'"}\
,stackTrace='${JexlerUtil.toSingleLine(stackTrace)}']"""
    }

}

