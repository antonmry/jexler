/*
   Copyright 2012 $(whois jexler.net)

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

package net.jexler.core;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The jexler suite implementation.
 *
 * @author $(whois jexler.net)
 */
public class JexlerSuiteImpl implements JexlerSuite {

    static final Logger log = LoggerFactory.getLogger(JexlerSuiteImpl.class);

    private final  Map<String,Jexler> jexlerMap;
    private final  List<Jexler> jexlers;

    public JexlerSuiteImpl(File suiteDir) {
        if (!suiteDir.isDirectory() || !suiteDir.exists()) {
            // TODO handle better
            throw new RuntimeException("File '" + suiteDir.getAbsolutePath() +
                    "' is not a directory or does not exist");
        }
        // LATER determine list when asked for
        // (but keep or shutdown jexlers that have disappeared)
        jexlerMap = new TreeMap<String,Jexler>();
        File[] files = suiteDir.listFiles();
        for (File file : files) {
            if (JexlerImpl.isJexlerDir(file)) {
                Jexler jexler = JexlerFactory.getJexler(file);
                jexlerMap.put(jexler.getId(), jexler);
            }
        }
        jexlers = new LinkedList<Jexler>();
        for (String id : jexlerMap.keySet()) {
            jexlers.add(jexlerMap.get(id));
        }
    }

    @Override
    public List<Jexler> getJexlers() {
        return jexlers;
    }

    @Override
    public Jexler getJexler(String id) {
        return jexlerMap.get(id);
    }

    @Override
    public void start() {
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            log.info("*** Jexler start: " + jexler.getId() + " (" + jexler.getDescription() + ")");
            jexler.start();
        }
    }

    @Override
    public void stop() {
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            log.info("*** Jexler stop: " + jexler.getId() + " (" + jexler.getDescription() + ")");
            jexler.stop();
        }
    }

}
