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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
public final class IssueFactoryTest
{
	
	@Test
    public void testBasic() throws Exception {
		Service service = MockService.getTestInstance();
		String message = "hi";
		Throwable cause = new RuntimeException();
		IssueFactory issueFactory = new IssueFactory();
		
		Issue issue = issueFactory.get(service, message, cause);
		assertNotNull("must not be null", issue);
		assertTrue("must be true", issue instanceof BasicIssue);
		assertEquals("must be same", service, issue.getService());
		assertEquals("must be same", "hi", issue.getMessage());
		assertEquals("must be same", cause, issue.getCause());
	}
	
}
