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

import net.jexler.test.FastTests;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class JexlerWorkaroundGroovy7407Test
{
    private void reset() {
        System.clearProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME);
        Jexler.WorkaroundGroovy7407.resetForUnitTests();
        WorkaroundGroovy7407WrappingGrapeEngine.setEngine(null);
    }

    @Before
    public void setup() {
        reset();
    }

    @After
    public void teardown() {
        reset();
    }

    @Test
    public void testConstructors() throws Exception {
        new Jexler.WorkaroundGroovy7407();
        new WorkaroundGroovy7407WrappingGrapeEngine("lock", null);
    }

    @Test
    public void testCompileOkWithWrapping() throws Exception {

        System.setProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME, "true");

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "test.groovy");

        Files.createFile(file.toPath());

        Jexler jexler = new Jexler(file, new JexlerContainer(dir));
        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());
        assertTrue("must be true", jexler.getIssues().isEmpty());
    }

    @Test
    public void testCompileFailsWithWrapping() throws Exception {

        System.setProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME, "true");

        File dir = Files.createTempDirectory(null).toFile();
        File file = new File(dir, "test.groovy");

        FileWriter writer = new FileWriter(file);
        writer.append("&%!+\n");
        writer.close();

        Jexler jexler = new Jexler(file, new JexlerContainer(dir));
        jexler.start();
        jexler.waitForStartup(10000);
        assertEquals("must be same", RunState.OFF, jexler.getRunState());

        assertEquals("must be same", 1, jexler.getIssues().size());
        Issue issue = jexler.getIssues().get(0);
        System.out.println(issue.toString());
        assertTrue("must be true",
                issue.getMessage().contains("Script compile failed."));
        assertEquals("must be same", jexler, issue.getService());
        assertNotNull("must not be null", issue.getCause());
        assertTrue("must be true", issue.getCause() instanceof CompilationFailedException);
    }

}
