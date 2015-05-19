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

package net.jexler.service

import ch.grengine.Grengine
import groovy.transform.CompileStatic
import net.jexler.JexlerUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service utilities.
 * Includes some static methods that might be useful in Groovy scripts
 * or in Java (for writing custom services).
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class ServiceUtil {

    private static final Logger log = LoggerFactory.getLogger(ServiceUtil.class)

    private static final Grengine gren = new Grengine()

    private static final String runnableWrappingJobScript = """\
        import groovy.transform.CompileStatic
        import org.quartz.*
        @CompileStatic
        class RunnableWrappingJob implements Job {
           public static Runnable runnable
          RunnableWrappingJob() {}
          void execute(JobExecutionContext context) throws JobExecutionException {
             runnable.run()
          }
        }
        """.stripIndent()

    static {
        gren.load(runnableWrappingJobScript)
    }

    static boolean waitForStartup(Service service, long timeout) {
        long t0 = System.currentTimeMillis()
        while (true) {
            if (!service.runState.busyStarting) {
                return true
            }
            if (System.currentTimeMillis() - t0 >= timeout) {
                return false
            }
            JexlerUtil.waitAtLeast(10)
        }
    }
    
    static boolean waitForShutdown(Service service, long timeout) {
           long t0 = System.currentTimeMillis()
        while (true) {
            if (service.off) {
                return true
            }
            if (System.currentTimeMillis() - t0 >= timeout) {
                return false
            }
            JexlerUtil.waitAtLeast(10)
        }
    }

    static Class newJobClassForRunnable(Runnable runnable) {
        Class jobClass = (Class)gren.load(gren.newDetachedLoader(), runnableWrappingJobScript)
        jobClass.getDeclaredField('runnable').set(null, runnable)
        return jobClass
    }

    /**
     * Convert "old-style" cron string to "quartz-style" cron with seconds
     * (and optionally with year), does not touch the cron string if not 5 items.
     */
    static String toQuartzStyleCron(String cron) {
        List<String> list = cron.trim().split(/\s/) as List<String>
        if (list.size() != 5) {
            return cron
        }
        list.add(0, '0') // seconds, on every full minute
        if (list[5] != '?' && list[3] != '?') {
            if (list[5] == '*') {
                list[5] = '?'
            } else if (list[3] == '*') {
                list[3] = '?'
            }
        }
        String cronNew = list.join(' ')
        log.trace("Translated old-style cron '$cron' to quartz-style cron with seconds '$cronNew'")
        return cronNew
    }

}
