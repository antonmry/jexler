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

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.util.concurrent.ConcurrentLinkedQueue

import net.jexler.service.Event
import net.jexler.service.Service

/**
 * Mock jexler implementation for unit tests.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class MockJexler extends Jexler {

    private final File file
    private final Queue<Event> events
    private final List<Issue> issues

    private MockJexler(File file) throws Exception {
        super(file, new JexlerContainer(file.parentFile))
        this.file = file
        events = new ConcurrentLinkedQueue<>()
        issues = new LinkedList<>()
    }

    MockJexler() throws Exception {
        this(new File(Files.createTempDirectory(null).toFile(), "Mock.groovy"))
    }

    @Override
    void start() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    boolean waitForStartup(long timeout) {
        throw new RuntimeException("Not implemented")
    }

    @Override
    void stop() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    boolean waitForShutdown(long timeout) {
        throw new RuntimeException("Not implemented")
    }

    @Override
    RunState getRunState() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    boolean isOn() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    boolean isOff() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    void trackIssue(Issue issue) {
        throw new RuntimeException("Not implemented")
    }

    @Override
    void trackIssue(Service service, String message, Throwable cause) {
        synchronized(issues) {
            issues.add(new Issue(service, message, cause))
        }
    }

    @Override
    List<Issue> getIssues() {
        synchronized(issues) {
            return issues
        }
    }

    @Override
    void forgetIssues() {
        synchronized(issues) {
            issues.clear()
        }
    }

    @Override
    void handle(Event event) {
        events.add(event)
    }

    @Override
    String getId() {
        return "Mock"
    }

    @Override
    File getFile() {
        return file
    }

    @Override
    File getDir() {
        return file.parentFile
    }

    @Override
    Map<String,Object> getMetaInfo() {
        throw new RuntimeException("Not implemented")
    }

    @Override
    JexlerContainer getContainer() {
        throw new RuntimeException("Not implemented")
    }

    /**
     * Wait at most timeout ms for event, return
     * event if got one in time, null otherwise.
     */
    Event takeEvent(long timeout) {
        long t0 = System.currentTimeMillis()
        while (true) {
            Event event = events.poll()
            if (event != null) {
                return event
            }
            if (System.currentTimeMillis() - t0 > timeout) {
                return null
            }
            try {
                Thread.sleep(10)
            } catch (InterruptedException e) {
            }
        }
    }

}
