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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All jexlers in a directory.
 *
 * @author $(whois jexler.net)
 */
public class Jexlers implements Service<Jexlers>, IssueTracker {

    static final Logger log = LoggerFactory.getLogger(Jexlers.class);

    /** Default timeout for stopping all jexlers in ms. */
    public static long DEFAULT_STOP_TIMEOUT = 5000;

    private File dir;
    private String id;

    /** key is jexler id (script file name with extension) */
    private final Map<String,Jexler> jexlerMap;
    private final List<Jexler> jexlers;

    private List<Issue> issues;

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
        this.dir = dir;
        id = dir.getName();
        jexlerMap = new TreeMap<String,Jexler>();
        jexlers = new LinkedList<Jexler>();
        issues = Collections.synchronizedList(new LinkedList<Issue>());
        refresh();
    }

    /**
     * Refresh list of jexlers.
     * Add new jexlers for new script files.
     * Remove old jexlers if their script file is gone and they are stopped.
     */
    public void refresh() {

        // list directory and create jexlers in map for new script files in directory
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile() && !file.isHidden()) {
                String id = Jexler.getIdForFile(file);
                if (!jexlerMap.containsKey(id)) {
                    Jexler jexler = new Jexler(file);
                    jexlerMap.put(jexler.getId(), jexler);
                }
            }
        }

        // recreate list while omitting jexlers without script file that are stopped
        jexlers.clear();
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            if (jexler.getFile().exists() || jexler.isRunning()) {
                jexlers.add(jexler);
            }
        }

        // recreate map with list entries
        jexlerMap.clear();
        for (Jexler jexler : jexlers) {
            jexlerMap.put(jexler.getId(), jexler);
        }
    }

    /**
     * Start jexlers that are marked as autostart.
     */
    public Jexlers autostart() {
        for (Jexler jexler : jexlers) {
            jexler.autostart();
        }
        return this;
    }

    @Override
    public Jexlers start() {
        for (Jexler jexler : jexlers) {
            jexler.start();
        }
        return this;
    }

    /**
     * Returns true if at least one jexler is running.
     */
    @Override
    public boolean isRunning() {
        for (Jexler jexler : jexlers) {
            if (jexler.isRunning()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop(long timeout) {
        for (Jexler jexler : jexlers) {
            jexler.stop(0);
        }

        long t0 = System.currentTimeMillis();
        while (isRunning() && System.currentTimeMillis() - t0 < timeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public void trackIssue(Issue issue) {
        log.error(issue.toString());
        issues.add(issue);
    }

    @Override
    public List<Issue> getIssues() {
        Collections.sort(issues);
        return issues;
    }

    @Override
    public void forgetIssues() {
        issues.clear();
    }


    @Override
    public String getId() {
        return id;
    }

    public File getDir() {
        return dir;
    }

    /**
     * Get jexlers, sorted by id (script file name).
     * @return
     */
    public List<Jexler> getJexlers() {
        return jexlers;
    }

    /**
     * Get jexler for given id (script file name).
     * @param id
     * @return jexler for given id or null if none
     */
    public Jexler getJexler(String id) {
        return jexlerMap.get(id);
    }

}
