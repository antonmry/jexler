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

import java.io.File;
import java.nio.file.Files;

import net.jexler.internal.BasicJexlerContainer;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class JexlerContainerFactoryTest
{

    @Test
    public void testBasic() throws Exception {
        File dir = Files.createTempDirectory(null).toFile();
        JexlerContainerFactory containerFactory = new JexlerContainerFactory();
        JexlerContainer container = containerFactory.get(dir);
        assertNotNull("must not be null", container);
        assertTrue("must be true", container instanceof BasicJexlerContainer);
        assertEquals("must be same", dir.getCanonicalPath(), container.getDir().getCanonicalPath());
        assertEquals("must be same", dir.getName(), container.getId());
    }

}
