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

package net.jexler.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import net.jexler.Issue;
import net.jexler.internal.MockJexler;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class DirWatchServiceTest
{
    private final static long MS_1_SEC = 1000;

    @Test
    public void testNoWatchDir() throws Exception {
        File watchDir = new File("does-not-exist");
        
        MockJexler jexler = new MockJexler();
        DirWatchService service = new DirWatchService(jexler, "watchid");
        service.setDir(watchDir);
        service.setSleepTimeMs(MS_1_SEC);
        
        service.start();
    	assertTrue("must be true", service.isOff());
        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        assertEquals("must be same", service, issue.getService());
        assertTrue("must be true",
        		issue.getMessage().startsWith("Could not create watch service or key"));
        assertNotNull("must not be null", issue.getException());
    	assertTrue("must be true", issue.getException() instanceof IOException);
    }

}
