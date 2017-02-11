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

import net.jexler.Jexler
import net.jexler.JexlerContainer
import net.jexler.JexlerUtil

import ch.qos.logback.classic.LoggerContext
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * Jexler context listener.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerContextListener implements ServletContextListener    {

    private static final Logger log = LoggerFactory.getLogger(JexlerContextListener.class)

    private static final String GUI_VERSION = '2.1.0' // IMPORTANT: keep in sync with version in main build.gradle

    static String jexlerTooltip
    static ServletContext servletContext
    static JexlerContainer container
    static File logfile

    static long startTimeout
    static long stopTimeout
    static boolean scriptAllowEdit
    static boolean scriptConfirmSave
    static boolean scriptConfirmDelete

    @Override
    void contextInitialized(ServletContextEvent event) {

        String coreVersion = Jexler.class.package.implementationVersion
        // no version in eclipse/unit tests (no jar with MANIFEST.MF)
        coreVersion = (coreVersion == null) ? '0.0.0' : coreVersion
        String groovyVersion = GroovyClassLoader.class.package.implementationVersion
        groovyVersion = (groovyVersion == null) ? '0.0.0' : groovyVersion
        jexlerTooltip = """\
          Jexler $GUI_VERSION
          • jexler-core: $coreVersion
          • Groovy: $groovyVersion
          https://www.jexler.net/""".stripIndent()
        log.info("Welcome to jexler.")
        log.info(JexlerUtil.toSingleLine(jexlerTooltip).replace('%n', ' | ').replace('• ', ''))
        servletContext = event.servletContext
        String webappPath = servletContext.getRealPath('/')
        container = new JexlerContainer(new File(webappPath, 'WEB-INF/jexlers'))
        container.start()

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
        startTimeout = param ? Long.parseLong(param) : 10000
        log.trace("jexler start timeout: $startTimeout ms")
        
        param = servletContext.getInitParameter('jexler.stop.timeout')
        stopTimeout = param ? Long.parseLong(param) : 10000
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

}
