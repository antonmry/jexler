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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.jexler.Issue;
import net.jexler.internal.BasicIssue;
import net.jexler.service.MockService;
import net.jexler.service.Service;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class BasicIssueTest
{

	@Test
    public void testBasic() throws Exception {
    	
		Issue issue = new BasicIssue(null, null, null);
		assertNull("must be null", issue.getService());
		assertNull("must be null", issue.getMessage());
		assertNull("must be null", issue.getCause());
		assertEquals("must be same", "", issue.getStackTrace());
		assertEquals("must be same",
				"Issue: [message=null,service=null,cause=null,stackTrace='']",
				issue.toString());

		MockService.setTestInstance(null,"mockid");
		Service service = MockService.getTestInstance();
		String serviceClass = MockService.class.getName();
		String message = "hi";
		Throwable cause = null;
		issue = new BasicIssue(service, message, cause);
		assertEquals("must be same", service, issue.getService());
		assertEquals("must be same", message, issue.getMessage());
		assertNull("must be null", issue.getCause());
		assertEquals("must be same", "", issue.getStackTrace());
		//System.out.println(issue);
		assertEquals("must be same",
				"Issue: [message='hi',service='" + serviceClass + ":mockid',"
						+ "cause=null,stackTrace='']", issue.toString());

		cause = new RuntimeException("run");
		issue = new BasicIssue(service, message, cause);
		assertEquals("must be same", service, issue.getService());
		assertEquals("must be same", message, issue.getMessage());
		assertEquals("must be same", cause, issue.getCause());
		assertNotNull("must not be null", issue.getStackTrace());
		//System.out.println(issue);
		assertTrue("must be true", issue.toString().startsWith(
				"Issue: [message='hi',service='" + serviceClass + ":mockid',"
						+ "cause='java.lang.RuntimeException: run',stackTrace='java.lang"));
		assertFalse("must be false", issue.toString().contains("\r"));
		assertFalse("must be false", issue.toString().contains("\n"));

		message = "got \r this \n and \r\n that \r\n\n\r .";
		issue = new BasicIssue(service, message, cause);
		System.out.println(issue);
		assertEquals("must be same", message, issue.getMessage());
		assertTrue("must be true", issue.toString().startsWith(
				"Issue: [message='got %n this %n and %n that %n%n%n .'"));
	}
}
