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

package net.jexler.war;

import net.jexler.test.DemoTests;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts embedded jetty with a jexler.
 *
 * @author $(whois jexler.net)
 */
@Category(DemoTests.class)
public class JexlerJettyTest {

	static final Logger log = LoggerFactory.getLogger(JexlerJettyTest.class);

	@Test
    public void demo() throws Exception {
    	System.setProperty("groovy.grape.report.downloads", "true");
        final int port = 9080;
        final Server server = new Server(port);
        WebAppContext wac = new WebAppContext();
        wac.setResourceBase("./src/main/webapp");
        wac.setDescriptor("WEB-INF/web.xml");
        wac.setContextPath("/");
        wac.setParentLoaderPriority(true);
        server.setHandler(wac);
        server.start();

        System.out.println();
        System.out.println("***************************************************************");
        System.out.println("Jexler in embedded jetty running on http://localhost:" + port + "/");
        System.out.println("Press ctrl-c to stop.");
        System.out.println("***************************************************************");
        
        server.join();
    }

}
