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
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;

import net.jexler.VerySlowTests;
import net.jexler.internal.MockJexler;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(VerySlowTests.class)
public final class DirWatchServiceTest
{
    private final static long MS_1_SEC = 1000;
    private final static long MS_30_SEC = 30000;
    
    /**
     * Takes about 5 minutes to complete.
     */
    @Test
    public void testDirWatch() throws Exception {
        
        Path watchDirPath = Files.createTempDirectory(null);
        
        MockJexler jexler = new MockJexler();
        DirWatchService dirWatchService = new DirWatchService(jexler, "watchid");
        dirWatchService.setDirPath(watchDirPath);
        dirWatchService.setSleepTimeMs(MS_1_SEC);
        assertEquals("must be same", "watchid", dirWatchService.getId());
        
        dirWatchService.start();
    	assertTrue("must be true", dirWatchService.isOn());
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
                
        checkEvents(jexler, dirWatchService, watchDirPath);
        
        dirWatchService.stop();
    	assertTrue("must be true", dirWatchService.waitForShutdown(MS_30_SEC));
        
        // create file after service stop
        File tempFile = new File(watchDirPath.toFile(), "temp2");
        FileWriter writer = new FileWriter(tempFile);
        writer.append("hello too");
        writer.close();
        
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // different watch directory
        watchDirPath = Files.createTempDirectory(null);
        dirWatchService.setDirPath(watchDirPath);
        
        dirWatchService.start();    
    	assertTrue("must be true", dirWatchService.isOn());
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
        
        checkEvents(jexler, dirWatchService, watchDirPath);
        
        // delete watch directory
        Files.delete(watchDirPath);
        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
        
        dirWatchService.stop();
    	assertTrue("must be true", dirWatchService.waitForShutdown(MS_30_SEC));
    }
        
    private void checkEvents(MockJexler jexler, DirWatchService dirWatchService,
            Path watchDirPath) throws Exception {

        // create file
        Path tempFilePath = new File(watchDirPath.toFile(), "temp").toPath();
        Files.createFile(tempFilePath);

        Event event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", dirWatchService, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        DirWatchEvent dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFilePath.toUri(),
                dirWatchEvent.getFilePath().toUri());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_CREATE,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // modify file
        Files.write(tempFilePath, "hello there".getBytes());

        event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", dirWatchService, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFilePath.toUri(),
                dirWatchEvent.getFilePath().toUri());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_MODIFY,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));

        // delete file
        Files.delete(tempFilePath);

        event = jexler.takeEvent(MS_30_SEC);
        assertNotNull("must not be null", event);
        assertEquals("must be same", dirWatchService, event.getService());
        assertTrue("must be true", event instanceof DirWatchEvent);
        dirWatchEvent = (DirWatchEvent)event;
        assertEquals("must be same", tempFilePath.toUri(),
                dirWatchEvent.getFilePath().toUri());
        assertEquals("must be same", StandardWatchEventKinds.ENTRY_DELETE,
                dirWatchEvent.getKind());

        assertNull("must be null", jexler.takeEvent(MS_30_SEC));
    }

}
