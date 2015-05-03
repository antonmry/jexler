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

import groovy.lang.GroovyClassLoader;
import net.jexler.Jexler;
import net.jexler.Jexlers;
import net.jexler.JexlersFactory;

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

    private static final String guiVersion = "1.0.14-SNAPSHOT"; // IMPORTANT: keep in sync with version in main build.gradle
	
	private static String version;
    private static ServletContext servletContext;
    private static Jexlers jexlers;
    private static File logfile;
    
    private static long startTimeout;
    private static long stopTimeout;
    private static boolean scriptAllowEdit;
    private static boolean scriptConfirmSave;
    private static boolean scriptConfirmDelete;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        String coreVersion = Jexler.class.getPackage().getImplementationVersion();
        // no version in eclipse/unit tests (no jar with MANIFEST.MF)
        coreVersion = (coreVersion == null) ? "0.0.0" : coreVersion;
        String groovyVersion = GroovyClassLoader.class.getPackage().getImplementationVersion();
        groovyVersion = (groovyVersion == null) ? "0.0.0" : groovyVersion;
        version = guiVersion + " (core " + coreVersion + " / groovy " + groovyVersion + ")";

        log.info("Welcome to jexler. Version: " + version);
        servletContext = event.getServletContext();
        String webappPath = servletContext.getRealPath("/");
        jexlers = new JexlersFactory().get(new File(webappPath, "WEB-INF/jexlers"));
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
        
    	String param = servletContext.getInitParameter("jexler.start.timeout");
    	startTimeout = 10000;
    	if (param != null) {
    		startTimeout = Long.parseLong(param);
    	}
    	log.trace("jexler start timeout: " + startTimeout + " ms");
        
    	param = servletContext.getInitParameter("jexler.stop.timeout");
    	stopTimeout = 10000;
    	if (param != null) {
    		stopTimeout = Long.parseLong(param);
    	}
    	log.trace("jexler stop timeout: " + stopTimeout + " ms");

    	param = servletContext.getInitParameter("jexler.security.script.allowEdit");
    	scriptAllowEdit = Boolean.parseBoolean(param);
    	log.trace("allow to edit jexler scripts: " + scriptAllowEdit);
    	
    	param = servletContext.getInitParameter("jexler.safety.script.confirmSave");
    	scriptConfirmSave = Boolean.parseBoolean(param);
    	log.trace("confirm jexler script save: " + scriptConfirmSave);
    	
    	param = servletContext.getInitParameter("jexler.safety.script.confirmDelete");
    	scriptConfirmDelete = Boolean.parseBoolean(param);
    	log.trace("confirm jexler script delete: " + scriptConfirmDelete);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        jexlers.stop();
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

    public static long getStartTimeout() {
    	return startTimeout;
    }

    public static long getStopTimeout() {
    	return stopTimeout;
    }
    
    public static boolean scriptAllowEdit() {
    	return scriptAllowEdit;
    }
    
    public static boolean scriptConfirmSave() {
    	return scriptConfirmSave;
    }
    
    public static boolean scriptConfirmDelete() {
    	return scriptConfirmDelete;
    }

}
