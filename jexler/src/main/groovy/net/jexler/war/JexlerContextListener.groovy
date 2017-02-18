/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

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
import ch.qos.logback.core.FileAppender
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * Jexler context listener.
 *
 * @author $(whois jexler.net"
 */
@CompileStatic
class JexlerContextListener implements ServletContextListener    {

    private static final Logger log = LoggerFactory.getLogger(JexlerContextListener.class)

    private static final String GUI_VERSION = '2.2.0-SNAPSHOT' // IMPORTANT: keep in sync with version in main build.gradle

    // Info tooltip with versions
    static String infoTooltip

    // Servlet context
    static ServletContext servletContext

    // The one and only jexler container in this webapp
    static JexlerContainer container

    // The logfile (by default one configured with level trace)
    static File logfile

    // Config items from web.xml
    static long startTimeoutSecs
    static long stopTimeoutSecs
    static boolean scriptAllowEdit
    static boolean scriptConfirmSave
    static boolean scriptConfirmDelete

    @Override
    void contextInitialized(ServletContextEvent event) {

        // Get and log versions (no versions in unit tests or IDE)
        String coreVersion = Jexler.class.package.implementationVersion
        coreVersion = (coreVersion == null) ? '0.0.0' : coreVersion
        String groovyVersion = GroovyClassLoader.class.package.implementationVersion
        groovyVersion = (groovyVersion == null) ? '0.0.0' : groovyVersion
        log.info("Welcome to jexler.")
        log.info("Jexler $GUI_VERSION | jexler-core: $coreVersion | Groovy: $groovyVersion")

        // Assemble info tooltip
        infoTooltip = """\
            user guide
            
            • jexler GUI: $GUI_VERSION
            • jexler-core: $coreVersion
            • Groovy: $groovyVersion""".stripIndent()

        // Set servlet context and set and start container
        servletContext = event.servletContext
        final String webappPath = servletContext.getRealPath('/')
        container = new JexlerContainer(new File(webappPath, 'WEB-INF/jexlers'))
        container.start()

        // Determine and set log file
        logfile = null
        final LoggerContext context = (LoggerContext)LoggerFactory.ILoggerFactory
        for (Logger logger : context.loggerList) {
            if (logger instanceof ch.qos.logback.classic.Logger) {
                ch.qos.logback.classic.Logger classicLogger = (ch.qos.logback.classic.Logger)logger
                classicLogger.iteratorForAppenders().each() { appender ->
                    if (appender instanceof FileAppender) {
                        logfile = new File(((FileAppender)appender).file)
                    }
                }
            }
        }
        log.trace("logfile: '$logfile.absolutePath'")

        // Get settings from files

        File settingsFile = new File(webappPath, 'WEB-INF/settings.groovy')
        Map settings = new ConfigSlurper('').parse(settingsFile.toURI().toURL()).flatten()
        File settingsCustomFile = new File(webappPath, 'WEB-INF/settings-custom.groovy')
        settings.putAll(new ConfigSlurper('').parse(settingsCustomFile.toURI().toURL()).flatten())
        log.trace("settings: $settings")

        startTimeoutSecs = (Long)settings."operation.jexler.startTimeoutSecs"
        stopTimeoutSecs = (Long)settings."operation.jexler.stopTimeoutSecs"
        scriptAllowEdit = (Boolean)settings."security.script.allowEdit"
        scriptConfirmSave = (Boolean)settings."safety.script.confirmSave"
        scriptConfirmDelete = (Boolean)settings."safety.script.confirmDelete"

        log.trace("jexler start timeout: $startTimeoutSecs secs")
        log.trace("jexler stop timeout: $stopTimeoutSecs secs")
        log.trace("allow to edit jexler scripts: $scriptAllowEdit")
        log.trace("confirm jexler script save: $scriptConfirmSave")
        log.trace("confirm jexler script delete: $scriptConfirmDelete")
    }

    @Override
    void contextDestroyed(ServletContextEvent event) {
        // Stop and close container
        container.stop()
        container.close()
        log.info('Jexler done.')
    }

}
