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
	
	private static String version;
    private static ServletContext servletContext;
    private static Jexlers jexlers;
    private static File logfile;
    private static long stopTimeout;
    private static boolean isScriptReadonly;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        version = JexlerContextListener.class.getPackage().getImplementationVersion();
        // no version in eclipse/unit tests (no jar with MANIFEST.MF)
        version = (version == null) ? "(DEVELOP)" : version;
        log.info("Welcome to jexler. Version: " + version);
        servletContext = event.getServletContext();
        String webappPath = servletContext.getRealPath("/");
        jexlers = new Jexlers(new File(webappPath, "WEB-INF/jexlers"));
        jexlers.autostart();

        // determine log file
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
        log.trace("logfile: '" + logfile.getAbsolutePath() + "'");
        
    	String param = servletContext.getInitParameter("jexler.stop.timeout");
    	stopTimeout = 10000;
    	if (param != null) {
    		stopTimeout = 1000 * Long.parseLong(param);
    	}
    	log.trace("jexler stop timeout: " + stopTimeout + " ms");

    	param = servletContext.getInitParameter("jexler.script.readonly");
    	isScriptReadonly = Boolean.parseBoolean(param);
    	log.trace("jexler scripts read only: " + isScriptReadonly);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        jexlers.stop(getStopTimeout());
        log.info("Jexler done.");
    }

    public static String getVersion() {
        return version;
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
    	return stopTimeout;
    }
    
    public static boolean isScriptReadonly() {
    	return isScriptReadonly;
    }

}
