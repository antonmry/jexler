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

import net.jexler.service.CronEvent
import net.jexler.service.DirWatchEvent
import net.jexler.service.MockService
import net.jexler.test.FastTests
import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class JexlerDispatcherSpec extends Specification {

    static class TestState {
        boolean declareCalled
        boolean startCalled
        boolean handleByClassNameAndServiceIdCalled
        boolean handleByClassNameCalled
        boolean handleCalled
        boolean stopCalled
    }

    static TestState testState

    def setup() {
        testState = new TestState()
    }
    
    def "minimal methods and no suitable event handler"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, "Test.groovy")
        file.text = """\
            //
            mockService = new MockService(jexler, 'MockService')
            JexlerDispatcher.dispatch(this)
            void start() {
              services.add(mockService)
              services.start()
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(10000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty

        when:
        def mockService = MockService.getInstance('MockService')

        then:
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0

        when:
        mockService.notifyJexler()
        JexlerUtil.waitAtLeast(1000)

        then:
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'Dispatch: No handler for event MockEvent from service MockService.'
        jexler.issues.first().cause == null
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0

        when:
        jexler.forgetIssues()

        then:
        jexler.issues.empty

        when:
        jexler.stop()
        jexler.waitForShutdown(10000)

        then:
        jexler.runState == RunState.OFF
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 0
        mockService.nStopped == 1
    }

    def "mandatory start method missing"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, "Test.groovy")
        file.text = """\
            //
            mockService = new MockService(jexler, 'MockService')
            JexlerDispatcher.dispatch(this)
            void noStartMethod() {
              services.add(mockService)
              services.start()
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(10000)

        then:
        jexler.runState == RunState.OFF
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'Dispatch: Mandatory start() method missing.'
        jexler.issues.first().cause == null
    }

    def "all methods and handlers"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, "Test.groovy")
        file.text = """\
            //
            JexlerDispatcher.dispatch(this)
            void declare() {
              mockService = new MockService(jexler, 'MockService')
              testState = JexlerDispatcherSpec.testState
              testState.declareCalled = true
            }
            void start() {
              services.add(mockService)
              services.start()
              testState.startCalled = true
            }
            void handleMockEventMockService(def event) {
              mockService.notifyGotEvent()
              testState.handleByClassNameAndServiceIdCalled = true
            }
            void handleCronEvent(def event) {
              mockService.notifyGotEvent()
              testState.handleByClassNameCalled = true
            }
            void handleDirWatchEvent(def event) {
              mockService.notifyGotEvent()
              testState.handleCalled = true
            }
            void stop() {
              testState.stopCalled = true
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(10000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty

        when:
        def mockService = MockService.getInstance('MockService')

        then:
        mockService.nStarted == 1
        mockService.nEventsSent == 0
        mockService.nEventsGotBack == 0
        mockService.nStopped == 0
        testState.declareCalled
        testState.startCalled
        !testState.handleByClassNameAndServiceIdCalled
        !testState.handleByClassNameCalled
        !testState.handleCalled
        !testState.stopCalled

        when:
        mockService.notifyJexler()
        JexlerUtil.waitAtLeast(1000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 1
        mockService.nEventsGotBack == 1
        mockService.nStopped == 0
        testState.declareCalled
        testState.startCalled
        testState.handleByClassNameAndServiceIdCalled
        !testState.handleByClassNameCalled
        !testState.handleCalled
        !testState.stopCalled

        when:
        mockService.notifyJexler(new CronEvent(jexler, "* * * * *"))
        JexlerUtil.waitAtLeast(1000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 2
        mockService.nEventsGotBack == 2
        mockService.nStopped == 0
        testState.declareCalled
        testState.startCalled
        testState.handleByClassNameAndServiceIdCalled
        testState.handleByClassNameCalled
        !testState.handleCalled
        !testState.stopCalled

        when:
        mockService.notifyJexler(new DirWatchEvent(jexler, new File('.'), StandardWatchEventKinds.ENTRY_CREATE))
        JexlerUtil.waitAtLeast(1000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 3
        mockService.nEventsGotBack == 3
        mockService.nStopped == 0
        testState.declareCalled
        testState.startCalled
        testState.handleByClassNameAndServiceIdCalled
        testState.handleByClassNameCalled
        testState.handleCalled
        !testState.stopCalled

        when:
        jexler.stop()
        jexler.waitForShutdown(10000)

        then:
        jexler.runState == RunState.OFF
        jexler.issues.empty
        mockService.nStarted == 1
        mockService.nEventsSent == 3
        mockService.nEventsGotBack == 3
        mockService.nStopped == 1
        testState.declareCalled
        testState.startCalled
        testState.handleByClassNameAndServiceIdCalled
        testState.handleByClassNameCalled
        testState.handleCalled
        testState.stopCalled
    }

    def "handle throws"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def file = new File(dir, "Test.groovy")
        file.text = """
            //
            JexlerDispatcher.dispatch(this)            
            void declare() {            
              mockService = new MockService(jexler, 'MockService')            
              testState = JexlerDispatcherSpec.testState
              testState.declareCalled = true            
            }            
            void start() {            
              services.add(mockService)            
              services.start()            
              testState.startCalled = true            
            }            
            void handleMockEventMockService(def event) {            
              mockService.notifyGotEvent()            
              testState.handleByClassNameAndServiceIdCalled = true            
              throw new RuntimeException('handle failed')            
            }            
            void stop() {            
              testState.stopCalled = true            
            }
            """
        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        jexler.waitForStartup(10000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.empty

        when:
        def mockService = MockService.getInstance('MockService')
        mockService.notifyJexler()
        JexlerUtil.waitAtLeast(1000)

        then:
        jexler.runState == RunState.IDLE
        jexler.issues.size() == 1
        jexler.issues.first().service == jexler
        jexler.issues.first().message == 'Dispatch: Handler handleMockEventMockService failed.'
        jexler.issues.first().cause instanceof RuntimeException
        jexler.issues.first().cause.message == 'handle failed'

        when:
        jexler.forgetIssues()
        jexler.stop()
        jexler.waitForShutdown(10000)

        then:
        jexler.runState == RunState.OFF
        jexler.issues.empty
    }

}
