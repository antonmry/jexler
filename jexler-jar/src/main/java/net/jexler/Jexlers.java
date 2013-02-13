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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A suite of jexlers.
 *
 * @author $(whois jexler.net)
 */
public class Jexlers {

    static final Logger log = LoggerFactory.getLogger(Jexlers.class);

    /** key is script file name */
    private final  Map<String,Jexler> jexlerMap;
    private final  List<Jexler> jexlers;

    /**
     * Constructor.
     * @param dir directory which contains jexler scripts
     */
    public Jexlers(File dir) {
        if (!dir.exists()) {
            throw new RuntimeException("Directory '" + dir.getAbsolutePath() + "' does not exist");
        } else  if (!dir.isDirectory()) {
            throw new RuntimeException("File '" + dir.getAbsolutePath() + "' is not a directory");
        }
        // LATER determine list when asked for it?
        // (but keep or stop jexlers that have disappeared?)
        jexlerMap = new TreeMap<String,Jexler>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Jexler jexler = new Jexler(file);
                jexlerMap.put(file.getName(), jexler);
            }
        }
        jexlers = new LinkedList<Jexler>();
        for (String name : jexlerMap.keySet()) {
            jexlers.add(jexlerMap.get(name));
        }

    }

    public void start() {
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            log.info("*** Jexler start: " + jexler.getName());
            jexler.start();
            if (!jexler.isRunning()) {
                log.error("Could not start jexler " + jexler.getName());
            }
        }
    }

    public void stop() {
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            log.info("*** Jexler stop: " + jexler.getName());
            jexler.stop();
        }
    }

    /**
     * Get jexlers, sorted by name.
     * @return
     */
    public List<Jexler> getJexlers() {
        return jexlers;
    }

    public Jexler getJexler(String name) {
        return jexlerMap.get(name);
    }

}
