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

package net.jexler;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.JexlerContainer;
import net.jexler.RunState;
import net.jexler.service.Event;
import net.jexler.service.Service;

/**
 * Mock jexler implementation for unit tests.
 *
 * @author $(whois jexler.net)
 */
public class MockJexler extends Jexler {

    private final File file;
    private final JexlerContainer container;
    private final Queue<Event> events;
    private final List<Issue> issues;

    public MockJexler(File file) throws Exception {
        super(new File("Dummy.groovy"), new JexlerContainer(new File(".")));
        this.file = file;
        File dir = Files.createTempDirectory(null).toFile();
        container = new JexlerContainer(dir);
        events = new ConcurrentLinkedQueue<>();
        issues = new LinkedList<>();
    }

    public MockJexler() throws Exception {
        this(null);
    }

    @Override
    public void start() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean waitForStartup(long timeout) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void stop() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean waitForShutdown(long timeout) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public RunState getRunState() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isOn() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isOff() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void trackIssue(Issue issue) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void trackIssue(Service service, String message, Throwable cause) {
        issues.add(new Issue(service, message, cause));
    }

    @Override
    public List<Issue> getIssues() {
        return issues;
    }

    @Override
    public void forgetIssues() {
        issues.clear();
    }

    public void addIssues(List<Issue> issues) {
        this.issues.addAll(issues);
    }

    @Override
    public void handle(Event event) {
        events.add(event);
    }

    @Override
    public String getId() {
        return "mockId";
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public File getDir() {
        return (file == null) ? null : file.getParentFile();
    }

    @Override
    public Map<String,Object> getMetaInfo() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public JexlerContainer getContainer() {
        return container;
    }

    /**
     * Wait at most timeout ms for event, return
     * event if got one in time, null otherwise.
     */
    public Event takeEvent(long timeout) {
        long t0 = System.currentTimeMillis();
        while (true) {
            Event event = events.poll();
            if (event != null) {
                return event;
            }
            if (System.currentTimeMillis() - t0 > timeout) {
                return null;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

}
