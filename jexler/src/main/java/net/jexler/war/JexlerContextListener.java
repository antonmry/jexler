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
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.jexler.Jexlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * Jexler context listener.
 *
 * @author $(whois jexler.net)
 */
public class JexlerContextListener implements ServletContextListener    {

	static final Logger log = LoggerFactory.getLogger(JexlerContextListener.class);

    private static ServletContext servletContext;
    private static Jexlers jexlers;
    private static File logfile;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String version = JexlerContextListener.class.getPackage().getImplementationVersion();
        // no version in eclipse/unit tests (no jar with MANIFEST.MF)
        log.info("Welcome to jexler. Version: " + (version == null ? "NONE" : version));
        servletContext = event.getServletContext();
        String webappPath = servletContext.getRealPath("/");
        jexlers = new Jexlers(new File(webappPath, "WEB-INF/jexlers"));
        jexlers.autostart();

        // LATER determine logfile more generally or simply configure in web.xml?
        logfile = null;
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        for (Logger logger : context.getLoggerList()) {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                ch.qos.logback.classic.Logger classicLogger = (ch.qos.logback.classic.Logger)logger;
                for (Iterator<Appender<ILoggingEvent>> index = classicLogger.iteratorForAppenders(); index.hasNext();) {
                    Appender<ILoggingEvent> appender = index.next();
                    if (appender instanceof ch.qos.logback.core.FileAppender) {
                        ch.qos.logback.core.FileAppender<?> fileAppender = (ch.qos.logback.core.FileAppender<?>)appender;
                        logfile = new File(fileAppender.getFile());
                    }
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        jexlers.stop(getStopTimeout());
        log.info("Jexler done.");
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static Jexlers getJexlers() {
        return jexlers;
    }

    public static File getLogfile() {
        return logfile;
    }

    public static long getStopTimeout() {
        // LATER configurable?
        return 10000;
    }

}
