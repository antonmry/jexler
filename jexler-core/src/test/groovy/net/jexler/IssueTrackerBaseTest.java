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
import static org.junit.Assert.assertTrue;

import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class IssueTrackerBaseTest {

    @Test
    public void testBasic() throws Exception {

        IssueTracker tracker = new IssueTrackerBase();
        assertTrue("must be true", tracker.getIssues().isEmpty());

        Issue issue = new Issue(null, "issue1", null);
        tracker.trackIssue(issue);
        assertEquals("must be same", 1, tracker.getIssues().size());
        assertEquals("must be same", "issue1", tracker.getIssues().get(0).getMessage());

        JexlerUtil.waitAtLeast(10);
        tracker.trackIssue(null, "issue2", null);
        assertEquals("must be same", 2, tracker.getIssues().size());
        assertEquals("must be same", "issue2", tracker.getIssues().get(0).getMessage());
        assertEquals("must be same", "issue1", tracker.getIssues().get(1).getMessage());

        tracker.forgetIssues();
        assertTrue("must be true", tracker.getIssues().isEmpty());
    }
}
