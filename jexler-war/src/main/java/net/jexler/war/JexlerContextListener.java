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

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.jexler.Jexlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jexler context listener.
 *
 * @author $(whois jexler.net)
 */
public class JexlerContextListener implements ServletContextListener    {

    static final Logger log = LoggerFactory.getLogger(JexlerContextListener.class);

    private static ServletContext servletContext;
    private static Jexlers jexlers;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        servletContext = event.getServletContext();
        String webappPath = servletContext.getRealPath("/");
        log.info("ServletContextName: " + servletContext.getServletContextName());
        log.info("webappPath: " + webappPath);
        jexlers = new Jexlers(new File(webappPath, "WEB-INF/jexlers"));
        jexlers.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        jexlers.stop(getStopTimeout());
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static Jexlers getJexlers() {
        return jexlers;
    }

    public static long getStopTimeout() {
        // TODO configurable?
        return 10000;
    }

}
