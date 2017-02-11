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

import net.jexler.service.ServiceState
import net.jexler.test.FastTests

import groovy.grape.Grape
import groovy.grape.GrapeEngine
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
class JexlerWorkaroundGroovy7407Spec extends Specification {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static void reset() throws Exception {
        System.clearProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME)
        Jexler.WorkaroundGroovy7407.resetForUnitTests()
        Jexler.WorkaroundGroovy7407WrappingGrapeEngine.engine = null
    }

    def setup() {
        reset()
    }

    def cleanup() {
        reset()
    }

    def 'TEST constructors'() {
        given:
        new Jexler.WorkaroundGroovy7407()
        new Jexler.WorkaroundGroovy7407WrappingGrapeEngine('lock', null)
    }

    def 'TEST compile with wrapping: compile ok'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'test.groovy')
        file.text = 'return 5'
        assert !(Grape.instance instanceof Jexler.WorkaroundGroovy7407WrappingGrapeEngine)
        System.setProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME, 'true')

        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, 10000)

        then:
        jexler.state == ServiceState.OFF
        jexler.issues.empty
        Grape.instance instanceof Jexler.WorkaroundGroovy7407WrappingGrapeEngine
    }

    def 'TEST compile with wrapping: compile fails'() {
        given:
        def dir = tempFolder.root
        def file = new File(dir, 'test.groovy')
        file.text = '&%!+'
        assert !(Grape.instance instanceof Jexler.WorkaroundGroovy7407WrappingGrapeEngine)
        System.setProperty(Jexler.WorkaroundGroovy7407.GRAPE_ENGINE_WRAP_PROPERTY_NAME, 'true')

        when:
        def jexler = new Jexler(file, new JexlerContainer(dir))
        jexler.start()
        JexlerUtil.waitForStartup(jexler, 10000)

        then:
        jexler.state == ServiceState.OFF
        jexler.issues.size() == 1
        jexler.issues.first().message == 'Script compile failed.'
        Grape.instance instanceof Jexler.WorkaroundGroovy7407WrappingGrapeEngine
    }

    def 'TEST shallow test of wrapping grape engine'() {
        when:
        def engine = new Jexler.WorkaroundGroovy7407WrappingGrapeEngine('lock', Mock(GrapeEngine))
        def testMap =  [ 'calleeDepth' : 3 ]
        
        then:
        engine.grab('') == null
        engine.grab([:]) == null
        engine.grab([:], [:]) == null
        engine.grab([:]) == null
        engine.grab(testMap, testMap) == null
        engine.enumerateGrapes() == null
        engine.resolve([:], [:]) == null
        engine.resolve(testMap, [:]) == null
        engine.resolve([:], [], [:]) == null
        engine.listDependencies(new GroovyClassLoader()) == null
        engine.addResolver([:]) == null
    }

}
