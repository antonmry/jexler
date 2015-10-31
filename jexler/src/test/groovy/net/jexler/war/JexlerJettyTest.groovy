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

package net.jexler.war

import groovy.transform.CompileStatic
import net.jexler.test.DemoTests

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Starts embedded jetty with a jexler.
 *
 * @author $(whois jexler.net)
 */
@Category(DemoTests.class)
@CompileStatic
class JexlerJettyTest {

    @Test
    void demo() throws Exception {
        System.setProperty('groovy.grape.report.downloads', 'true')
        final int port = 9080
        final Server server = new Server(port)
        WebAppContext wac = new WebAppContext()
        wac.resourceBase = './src/main/webapp'
        wac.descriptor = 'WEB-INF/web.xml'
        wac.contextPath = '/'
        wac.parentLoaderPriority = true
        server.handler = wac
        server.start()

        println()
        println('***************************************************************')
        println("Jexler in embedded jetty running on http://localhost:$port/")
        println('Press ctrl-c to stop.')
        println('***************************************************************')
        
        server.join()
    }

}
