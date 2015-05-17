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

import net.jexler.service.StopEvent
import net.jexler.test.FastTests
import org.junit.experimental.categories.Category
import spock.lang.Specification

import java.nio.file.Files

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
class JexlerContainerSpec extends Specification {

    private final static long MS_1_SEC = 1000
    private final static long MS_10_SEC = 10000

    def "main functionality in detail"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def jexlerBody = """\
            while (true) {
              event = events.take()
              if (event instanceof StopEvent) {
                return
              }
            }
            """
        new File(dir, 'Jexler1.groovy').text = "[ 'autostart' : false ]\n$jexlerBody"
        new File(dir, 'Jexler2.groovy').text = "[ 'autostart' : true ]\n$jexlerBody"
        new File(dir, 'Jexler3.groovy').text = "[ 'autostart' : false ]\n$jexlerBody"
        new File(dir, 'Jexler4.script').text = "foo.bar=xyz"

        when:
        def container = new JexlerContainer(dir)

        then:
        container.runState == RunState.OFF
        container.off
        container.dir == dir
        container.id == dir.name
        container.jexlers.size() == 3
        container.issues.empty

        when:
        def jexler1 = container.getJexler('Jexler1')
        def jexler2 = container.getJexler('Jexler2')
        def jexler3 = container.getJexler('Jexler3')

        then:
        jexler1.id == 'Jexler1'
        jexler2.id == 'Jexler2'
        jexler3.id == 'Jexler3'
        jexler1.runState == RunState.OFF
        jexler2.runState == RunState.OFF
        jexler3.runState == RunState.OFF

        when:
        container.start()
        container.waitForStartup(MS_10_SEC)

        then:
        container.runState == RunState.IDLE
        container.on
        jexler1.runState == RunState.IDLE
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.IDLE
        container.issues.empty

        when:
        container.stop()
        container.waitForShutdown(MS_10_SEC)

        then:
        container.runState == RunState.OFF
        jexler1.runState == RunState.OFF
        jexler2.runState == RunState.OFF
        jexler3.runState == RunState.OFF
        container.issues.empty

        when:
        container.autostart()
        container.waitForStartup(MS_10_SEC)

        then:
        container.runState == RunState.IDLE
        container.on
        jexler1.runState == RunState.OFF
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.OFF
        container.issues.empty

        when:
        container.start()
        container.waitForStartup(MS_10_SEC)

        then:
        container.runState == RunState.IDLE
        container.on
        jexler1.runState == RunState.IDLE
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.IDLE
        container.issues.empty

        when:
        jexler3.handle(new StopEvent(jexler3))
        JexlerUtil.waitAtLeast(MS_1_SEC)

        then:
        container.runState == RunState.IDLE
        container.on
        jexler1.runState == RunState.IDLE
        jexler2.runState == RunState.IDLE
        jexler3.runState == RunState.OFF
        container.issues.empty

        when:
        // delete file for Jexler2
        assert jexler2.file.delete()

        then:
        container.getJexler('Jexler2') == jexler2
        container.jexlers.size() == 3

        when:
        // don't remove running jexler even if file is gone
        container.refresh()

        then:
        container.getJexler('Jexler2') == jexler2
        container.jexlers.size() == 3

        when:
        container.stop()
        container.waitForShutdown(MS_10_SEC)

        then:
        container.runState == RunState.OFF
        jexler1.runState == RunState.OFF
        jexler2.runState == RunState.OFF
        jexler3.runState == RunState.OFF
        container.issues.empty
        container.jexlers.size() == 3

        when:
        // now after stopping must remove Jexler2
        container.refresh()

        then:
        container.runState == RunState.OFF
        container.issues.empty
        container.jexlers.size() == 2

        when:
        container.start()
        container.waitForStartup(MS_10_SEC)

        then:
        container.runState == RunState.IDLE
        container.issues.empty
        container.jexlers.size() == 2

        when:
        container.trackIssue(null, 'some issue', new RuntimeException())

        then:
        container.issues.size() == 1
        container.issues.first().service == null
        container.issues.first().message == 'some issue'
        container.issues.first().cause instanceof RuntimeException

        when:
        container.forgetIssues()

        then:
        container.issues.empty

        when:
        container.stop()
        container.waitForShutdown(MS_10_SEC)

        then:
        container.off
    }

    def "constructor throws because directory does not exist"() {
        when:
        def dir = new File('does-not-exist')
        new JexlerContainer(dir)

        then:
        RuntimeException e = thrown()
        e.message == "Directory '${dir.absolutePath}' does not exist."
    }

    def "constructor throws because file is not a directory"() {
        when:
        def file = Files.createTempFile(null, ".tmp").toFile()
        new JexlerContainer(file)

        then:
        RuntimeException e = thrown()
        e.message == "File '${file.absolutePath}' is not a directory."
    }

    def "get jexler id"() {
        when:
        def dir = Files.createTempDirectory(null).toFile()
        def container = new JexlerContainer(dir)

        then:
        container.getJexlerId(new File(dir, 'Foo.groovy')) == 'Foo'
        container.getJexlerId(new File('Foo.groovy'))      == 'Foo'
        container.getJexlerId(new File('Foo.java'))        == null
        container.getJexlerId(new File('Foo.java.groovy')) == 'Foo.java'
        container.getJexlerId(new File('Foo.groovy.java')) == null
    }

    def "get jexler file"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def container = new JexlerContainer(dir)

        when:
        def file = container.getJexlerFile('Foo')

        then:
        file.canonicalPath == new File(dir, 'Foo.groovy').canonicalPath
    }

    def "shared scheduler and close"() {
        given:
        def dir = Files.createTempDirectory(null).toFile()
        def container = new JexlerContainer(dir)

        when:
        def scheduler1 = container.getSharedScheduler()
        def scheduler2 = container.getSharedScheduler()

        then:
        // must be same reference
        scheduler1.is(scheduler2)
        scheduler1.started

        when:
        container.close()
        def scheduler3 = container.getSharedScheduler()

        then:
        // must get a new reference
        !scheduler1.is(scheduler3)
        !scheduler1.started
        scheduler3.started

        when:
        container.close()

        then:
        !scheduler3.started
    }

}
