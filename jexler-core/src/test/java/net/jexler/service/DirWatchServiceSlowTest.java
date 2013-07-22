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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;

import net.jexler.internal.MockJexler;
import net.jexler.test.SlowTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(SlowTests.class)
public final class DirWatchServiceSlowTest
{
    private final static long MS_1_SEC = 1000;
    private final static long MS_30_SEC = 30000;
    
    /**
     * Takes about 5 minutes to complete.
     */
    @Test
    public void testBasic() throws Exception {
        
        File watchDir = Files.createTempDirectory(null).toFile();
        
        MockJexler jexler = new MockJexler();
        DirWatchService service = new DirWatchService(jexler, "watchid");
        service.setDir(watchDir);
        service.setSleepTimeMs(MS_1_SEC);
        assertEquals("must be same", "watchid", service.getId());
        
        service.start();
    	assertTrue("must be true", service.isOn());
    	assertTrue("must be true", service.waitForStartup(MS_30_SEC));
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
                
        checkEvents(jexler, service, watchDir);
        
        service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_30_SEC));
        
        // create file after service stop
        File tempFile = new File(watchDir, "temp2");
        FileWriter writer = new FileWriter(tempFile);
        writer.append("hello too");
        writer.close();
        
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // different watch directory
        watchDir = Files.createTempDirectory(null).toFile();
        service.setDir(watchDir);
        
        service.start();    
    	assertTrue("must be true", service.isOn());
    	assertTrue("must be true", service.waitForStartup(MS_30_SEC));
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
        
        service.start();    
    	assertTrue("must be true", service.getRunState().isIdle());

    	checkEvents(jexler, service, watchDir);
        
        // delete watch directory
        assertTrue("must be true", watchDir.delete());
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
        
        service.stop();
    	assertTrue("must be true", service.waitForShutdown(MS_30_SEC));
        
        service.stop();
    	assertTrue("must be true", service.isOff());
    }

    private void checkEvents(MockJexler jexler, DirWatchService service,
            File watchDir) throws Exception {

        // create file
        File tempFile = new File(watchDir, "temp");
        Files.createFile(tempFile.toPath());

        Event event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", service, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        DirWatchEvent dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFile.getCanonicalPath(),
                dirWatchEvent.getFile().getCanonicalPath());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_CREATE,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // modify file
        FileWriter writer = new FileWriter(tempFile);
        writer.append("hello there");
        writer.close();

        event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", service, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFile.getCanonicalPath(),
                dirWatchEvent.getFile().getCanonicalPath());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_MODIFY,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // delete file
        Files.delete(tempFile.toPath());

        event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", service, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFile.getCanonicalPath(),
                dirWatchEvent.getFile().getCanonicalPath());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_DELETE,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
    }

}
