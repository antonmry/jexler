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

package net.jexler

import net.jexler.service.MockService
import net.jexler.test.FastTests
import org.codehaus.groovy.control.CompilationFailedException
import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.Files

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class JexlerSpec extends Specification {

    private final static long MS_1_SEC = 1000
    private final static long MS_10_SEC = 10000

    def "script simple run (exit immediately or silently not even started if not Script instance)"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')

        expect:
        file.setText(text)
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)
        jexler.dir.absolutePath == dir.absolutePath
        jexler.file.absolutePath == file.absolutePath
        jexler.id == 'Test'
        jexler.runState == RunState.OFF
        jexler.off
        jexler.metaInfo.size() == metaInfoSize
        jexler.issues.empty

        where:
        metaInfoSize | text
        0            | ''
        0            | 'return 5'
        1            | "['autostart' : true]"
        0            | 'class NotInstanceOfScript {}'
    }

    def "script compile, create or run fails"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')

        expect:
        file.setText(text)
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)
        jexler.runState == RunState.OFF
        jexler.off
        jexler.metaInfo.size() == metaInfoSize
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == message
        causeClass.isAssignableFrom(jexler.issues.first().cause.class)

        where:
        metaInfoSize | message                  | causeClass
        2            | 'Script compile failed.' | CompilationFailedException.class
        0            | 'Script create failed.'  | ExceptionInInitializerError.class
        2            | 'Script run failed.'     | IllegalArgumentException.class
        0            | 'Script run failed.'     | FileNotFoundException.class
        1            | 'Script run failed.'     | NoClassDefFoundError.class

        text << [
                """\
                    [ 'autostart' : false, 'foo' : 'bar' ]
                    # does not compile...
                """,
                """\
                    public class Test extends Script {
                      static { throw new RuntimeException() }
                      public def run() {}
                    }
                """,
                """\
                    [ 'autostart' : false, 'foo' : 'bar' ]
                    throw new IllegalArgumentException()
                """,
                """\
                    []
                    throw new FileNotFoundException()
                    """,
                """\
                    [ 'autostart' : true ]
                    throw new NoClassDefFoundError()
                """
        ]
    }

    def "simple jexler script life cycle"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autostart' : false, 'foo' : 'bar' ]
            def mockService = new MockService(jexler, 'mock-service')
            services.add(mockService)
            services.start()
            while (true) {
              event = events.take()
              if (event instanceof MockEvent) {
                mockService.notifyGotEvent()
              } else if (event instanceof StopEvent) {
                return
              }
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))

        then:
        jexler.runState == RunState.OFF
        jexler.off
        jexler.metaInfo.size() == 2
        jexler.metaInfo.autostart == false
        jexler.metaInfo.foo == 'bar'
        jexler.issues.empty

        when:
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')

        then:
        jexler.runState == RunState.IDLE
        jexler.on
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0

        when:
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)

        then:
        jexler.runState == RunState.IDLE
        jexler.on
        jexler.issues.empty

        when:
        mockService.notifyJexler()
        JexlerUtil.waitAtLeast(MS_1_SEC)

        then:
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 1
        mockService.nStopped == 0

        when:
        def ex = new RuntimeException()
        jexler.trackIssue(mockService, "mock issue", ex)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == mockService
        jexler.issues.first().message == "mock issue"
        jexler.issues.first().cause == ex

        when:
        jexler.forgetIssues()

        then:
        jexler.issues.empty

        when:
        jexler.stop()
        jexler.waitForShutdown(MS_10_SEC)

        then:
        jexler.runState == RunState.OFF
        jexler.off
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 1
        mockService.nStopped == 1

        when:
        jexler.stop()
        jexler.waitForShutdown(MS_10_SEC)

        then:
        jexler.runState == RunState.OFF
        jexler.off
        jexler.issues.empty
    }

    def "runtime exception at jexler shutdown"() {

        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autostart' : false, 'foo' : 'bar' ]
            def mockService = new MockService(jexler, 'mock-service')
            mockService.setStopRuntimeException(new RuntimeException())
            services.add(mockService)
            services.start()
            while (true) {
              event = events.take()
              if (event instanceof MockEvent) {
                mockService.notifyGotEvent()
              } else if (event instanceof StopEvent) {
                return
              }
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')

        then:
        jexler.runState == RunState.IDLE
        jexler.on
        jexler.issues.empty

        when:
        jexler.stop()
        jexler.waitForShutdown(MS_10_SEC)

        then:
        jexler.runState == RunState.OFF
        jexler.off
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Could not stop services.'
        jexler.issues.first().cause == mockService.stopRuntimeException
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 1

    }

    def "meta info: no file"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')

        when:
        // jexler file does not exist
        def jexler = new Jexler(file, new JexlerContainer(dir))

        then:
        jexler.issues.empty

        when:
        def metaInfo = jexler.metaInfo

        then:
        metaInfo.isEmpty()
        jexler.issues.empty
    }

    def "meta info: IOException while reading file"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()

        when:
        // passing dir as jexler file
        def jexler = new Jexler(dir, new JexlerContainer(dir))

        then:
        jexler.issues.empty

        when:
        jexler.metaInfo

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message.startsWith('Could not read meta info from jexler file')
        jexler.issues.first().cause instanceof IOException
    }

    def "meta info: default to empty"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')

        expect:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.metaInfo.isEmpty()

        where:
        text << [ '', "[ 'not-a-map' ]", '#does not compile' ]
    }
    
    def "interrupt event take in jexler event loop"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autoimport' : false ]
            while (true) {
              event = events.take()
              if (event instanceof net.jexler.service.StopEvent) {
                return
              }
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(MS_10_SEC)

        then:
        jexler.issues.empty

        when:
        // find script thread
        Thread scriptThread = null
        Thread.allStackTraces.each() { thread, stackTrace ->
            if (thread.name == 'Test') {
                scriptThread = thread
            }
        }

        then:
        scriptThread

        when:
        scriptThread.interrupt()
        JexlerUtil.waitAtLeast(MS_1_SEC)

        then:
        jexler.runState == RunState.IDLE
        jexler.on
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'Could not take event.'
        jexler.issues.first().cause instanceof InterruptedException

        jexler.stop()
        jexler.waitForShutdown(MS_10_SEC)
    }

}