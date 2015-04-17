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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.jexler.Issue;
import net.jexler.IssueTracker;
import net.jexler.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic default implementation of issue tracker interface.
 *
 * @author $(whois jexler.net)
 */
public class BasicIssueTracker implements IssueTracker {

	static final Logger log = LoggerFactory.getLogger(BasicIssueTracker.class);

	private final List<Issue> issues;
	
	/**
	 * Default constructor.
	 */
	public BasicIssueTracker() {
		issues = Collections.synchronizedList(new LinkedList<Issue>());
	}
	
    @Override
    public void trackIssue(Issue issue) {
        log.error(issue.toString());
        issues.add(issue);
    }

    @Override
    public void trackIssue(Service service, String message, Throwable cause) {
    	trackIssue(new BasicIssue(service, message, cause));
    }

    @Override
    public List<Issue> getIssues() {
        Collections.sort(issues);
        return issues;
    }

    @Override
    public void forgetIssues() {
        issues.clear();
    }

}
