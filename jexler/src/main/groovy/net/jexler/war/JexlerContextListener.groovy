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
import net.jexler.JexlerUtil

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import net.jexler.Jexler
import net.jexler.JexlerContainer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender

/**
 * Jexler context listener.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerContextListener implements ServletContextListener    {

    private static final Logger log = LoggerFactory.getLogger(JexlerContextListener.class)

    private static final String guiVersion = '2.0.3' // IMPORTANT: keep in sync with version in main build.gradle

    private static String jexlerTooltip
    private static ServletContext servletContext
    private static JexlerContainer container
    private static File logfile
    
    private static long startTimeout
    private static long stopTimeout
    private static boolean scriptAllowEdit
    private static boolean scriptConfirmSave
    private static boolean scriptConfirmDelete

    @Override
    void contextInitialized(ServletContextEvent event) {

        String coreVersion = Jexler.class.package.implementationVersion
        // no version in eclipse/unit tests (no jar with MANIFEST.MF)
        coreVersion = (coreVersion == null) ? '0.0.0' : coreVersion
        String groovyVersion = GroovyClassLoader.class.package.implementationVersion
        groovyVersion = (groovyVersion == null) ? '0.0.0' : groovyVersion
        jexlerTooltip = """\
Jexler $guiVersion
• jexler-core: $coreVersion
• Groovy: $groovyVersion
https://www.jexler.net/\
"""
        log.info("Welcome to jexler.")
        log.info(JexlerUtil.toSingleLine(jexlerTooltip).replace('%n', ' / '))
        servletContext = event.servletContext
        String webappPath = servletContext.getRealPath('/')
        container = new JexlerContainer(new File(webappPath, 'WEB-INF/jexlers'))
        container.autostart()

        // determine log file
        logfile = null
        LoggerContext context = (LoggerContext)LoggerFactory.ILoggerFactory
        for (Logger logger : context.loggerList) {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                ch.qos.logback.classic.Logger classicLogger = (ch.qos.logback.classic.Logger)logger
                classicLogger.iteratorForAppenders().each() { appender ->
                    if (appender instanceof ch.qos.logback.core.FileAppender) {
                        logfile = new File(((ch.qos.logback.core.FileAppender)appender).file)
                    }
                }
            }
        }
        log.trace("logfile: '$logfile.absolutePath'")
        
        String param = servletContext.getInitParameter('jexler.start.timeout')
        startTimeout = 10000
        if (param != null) {
            startTimeout = Long.parseLong(param)
        }
        log.trace("jexler start timeout: $startTimeout ms")
        
        param = servletContext.getInitParameter('jexler.stop.timeout')
        stopTimeout = 10000
        if (param != null) {
            stopTimeout = Long.parseLong(param)
        }
        log.trace("jexler stop timeout: $stopTimeout ms")

        param = servletContext.getInitParameter('jexler.security.script.allowEdit')
        scriptAllowEdit = Boolean.parseBoolean(param)
        log.trace("allow to edit jexler scripts: $scriptAllowEdit")

        param = servletContext.getInitParameter('jexler.safety.script.confirmSave')
        scriptConfirmSave = Boolean.parseBoolean(param)
        log.trace("confirm jexler script save: $scriptConfirmSave")

        param = servletContext.getInitParameter('jexler.safety.script.confirmDelete')
        scriptConfirmDelete = Boolean.parseBoolean(param)
        log.trace("confirm jexler script delete: $scriptConfirmDelete")
    }

    @Override
    void contextDestroyed(ServletContextEvent event) {
        container.stop()
        container.close()
        log.info('Jexler done.')
    }

    static String getJexlerTooltip() {
        return jexlerTooltip
    }

    static ServletContext getServletContext() {
        return servletContext
    }

    static JexlerContainer getContainer() {
        return container
    }

    static File getLogfile() {
        return logfile
    }

    static long getStartTimeout() {
        return startTimeout
    }

    static long getStopTimeout() {
        return stopTimeout
    }
    
    static boolean scriptAllowEdit() {
        return scriptAllowEdit
    }
    
    static boolean scriptConfirmSave() {
        return scriptConfirmSave
    }
    
    static boolean scriptConfirmDelete() {
        return scriptConfirmDelete
    }

}
