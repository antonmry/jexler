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

package net.jexler

import net.jexler.service.MockEvent
import net.jexler.service.MockService
import net.jexler.service.ServiceBase
import net.jexler.service.ServiceState
import net.jexler.service.StopEvent
import net.jexler.test.FastTests

import org.codehaus.groovy.control.CompilationFailedException
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class JexlerSpec extends Specification {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final static long MS_1_SEC = 1000
    private final static long MS_10_SEC = 10000

    def 'TEST script simple run (exit immediately or silently not even started if not Script instance)'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')

        expect:
        file.setText(text)
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        jexler.dir.absolutePath == dir.absolutePath
        jexler.file.absolutePath == file.absolutePath
        jexler.id == 'Test'
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.metaInfo.size() == metaInfoSize
        jexler.issues.empty

        where:
        metaInfoSize | text
        0            | ''
        0            | 'return 5'
        1            | "['autostart' : true]"
        0            | 'class NotInstanceOfScript {}'
    }

    def 'TEST script cannot read meta info'() {
        when:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        def jexler = new Jexler(file, new JexlerContainer(dir))

        then:
        // create directory with name of jexler script file
        file.mkdir()

        when:
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == "Could not read meta info from jexler file '$file.absolutePath'."
        jexler.issues.first().cause instanceof IOException
    }

    def 'TEST script compile, create or run fails'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')

        expect:
        file.setText(text)
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        jexler.state == ServiceState.OFF
        jexler.state.off
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
                """.stripIndent(),
                """\
                    public class Test extends Script {
                      static { throw new RuntimeException() }
                      public def run() {}
                    }
                """.stripIndent(),
                """\
                    [ 'autostart' : false, 'foo' : 'bar' ]
                    throw new IllegalArgumentException()
                """.stripIndent(),
                """\
                    []
                    throw new FileNotFoundException()
                """.stripIndent(),
                """\
                    [ 'autostart' : true ]
                    throw new NoClassDefFoundError()
                """.stripIndent()
        ]
    }

    def 'TEST simple jexler script life cycle'() {
        given:
        def dir = tempFolder.root
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
            """.stripIndent()
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.metaInfo.size() == 2
        jexler.metaInfo.autostart == false
        jexler.metaInfo.foo == 'bar'
        jexler.issues.empty

        when:
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')

        then:
        jexler.state == ServiceState.IDLE
        jexler.state.on
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0
        mockService.nZapped == 0

        when:
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.IDLE
        jexler.state.on
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
        mockService.nZapped == 0

        when:
        jexler.stop()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 1
        mockService.nStopped == 1
        mockService.nZapped == 0

        when:
        jexler.stop()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.issues.empty
    }

    def 'TEST zap hanging jexler'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autostart' : false, 'foo' : 'bar' ]
            def mockService = new MockService(jexler, 'mock-service')
            services.add(mockService)
            services.start()
            while (true) {
              event = events.take()
              if (event instanceof MockEvent) {
                while(true) {}
              } else if (event instanceof StopEvent) {
                return
              }
            }
            """.stripIndent()
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.metaInfo.size() == 2
        jexler.metaInfo.autostart == false
        jexler.metaInfo.foo == 'bar'
        jexler.issues.empty

        when:
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')

        then:
        jexler.state == ServiceState.IDLE
        jexler.state.on
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0
        mockService.nZapped == 0

        when:
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.IDLE
        jexler.state.on
        jexler.issues.empty

        when:
        mockService.notifyJexler()
        JexlerUtil.waitAtLeast(MS_1_SEC)

        then:
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0
        mockService.nZapped == 0

        when:
        jexler.zap()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)
        JexlerUtil.waitAtLeast(MS_10_SEC)

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Script run failed.'
        jexler.issues.first().cause.class == java.lang.ThreadDeath
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 0
        // called a second time because stopping thread throws
        mockService.nStopped == 2
        mockService.nZapped == 1
    }

    def 'TEST detect stop events in queue'() {
        given:
        def events = new Jexler.Events()
        def service = new ServiceBase('testid') {
            void start() {}
            void stop() {}
            void zap() {}
        }
        when:
        // empty queue, need a dummy statement
        events.size()

        then:
        !events.nextIsStop()
        !events.hasStop()

        when:
        events.put(new MockEvent(service))

        then:
        !events.nextIsStop()
        !events.hasStop()

        when:
        events.put(new StopEvent(service))

        then:
        !events.nextIsStop()
        events.hasStop()

        when:
        events.poll()

        then:
        events.nextIsStop()
        events.hasStop()

        when:
        events.poll()

        then:
        !events.nextIsStop()
        !events.hasStop()
    }

    def 'TEST track issue'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        def jexler = new Jexler(file, new JexlerContainer(dir))

        when:
        def mockService = MockService.getInstance('mock-service')
        def e = new RuntimeException()
        jexler.trackIssue(mockService, 'mock issue', e)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == mockService
        jexler.issues.first().message == 'mock issue'
        jexler.issues.first().cause == e

        when:
        jexler.forgetIssues()

        then:
        jexler.issues.empty

        when:
        def t = new Throwable()
        jexler.trackIssue(new Issue(jexler, 'jexler issue', t))

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'jexler issue'
        jexler.issues.first().cause == t

        when:
        jexler.forgetIssues()

        then:
        jexler.issues.empty
    }

    def 'TEST runtime exception at jexler shutdown'() {

        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autostart' : false, 'foo' : 'bar' ]
            def mockService = new MockService(jexler, 'mock-service')
            mockService.stopRuntimeException = new RuntimeException()
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
            """.stripIndent()

        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')

        then:
        jexler.state == ServiceState.IDLE
        jexler.state.on
        jexler.issues.empty

        when:
        jexler.stop()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Could not stop services.'
        jexler.issues.first().cause == mockService.stopRuntimeException
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 1
    }

    def 'TEST access to jexlerBinding in other classes and jexler itself'() {

        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autostart' : false, 'foo' : 'bar' ]
            def mockService = new MockService(jexlerBinding.jexler, 'mock-service')
            services.add(mockService)
            services.start()
            while (true) {
              event = events.take()
              if (event instanceof MockEvent) {
                mockService.notifyGotEvent()
                Util.writeId()
              } else if (event instanceof StopEvent) {
                return
              }
            }
            """.stripIndent()
        def fileUtil = new File(dir, 'Util.groovy')
        fileUtil.text = """\
            class Util {
              static def id = jexlerBinding.jexler.id
              static def writeId() {
                new File(jexlerBinding.jexler.dir, 'out').setText(id)
              }
            }
            """.stripIndent()

        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)
        def mockService = MockService.getInstance('mock-service')
        mockService.notifyJexler()
        jexler.stop()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)

        then:
        jexler.state == ServiceState.OFF
        jexler.state.off
        jexler.issues.size() == 0
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 1
        mockService.nStopped == 1
        new File(dir, 'out').text == 'Test'
    }

    def 'TEST meta info: no file'() {
        given:
        def dir = tempFolder.root
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

    def 'TEST meta info: IOException while reading file'() {
        given:
        def dir = tempFolder.root

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

    def 'TEST meta info: default to empty'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')

        expect:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.metaInfo.isEmpty()

        where:
        text << [ '', "[ 'not-a-map' ]", '#does not compile' ]
    }
    
    def 'TEST interrupt event take in jexler event loop'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'Test.groovy')
        file.text = """\
            [ 'autoimport' : false ]
            while (true) {
              event = events.take()
              if (event instanceof net.jexler.service.StopEvent) {
                return
              }
            }
            """.stripIndent()
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, MS_10_SEC)

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
        jexler.state == ServiceState.IDLE
        jexler.state.on
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'Could not take event.'
        jexler.issues.first().cause instanceof InterruptedException

        jexler.stop()
        JexlerUtil.waitForShutdown(jexler, MS_10_SEC)
    }

}
