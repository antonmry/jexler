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
public class Jexlers implements Service, IssueTracker {

	static final Logger log = LoggerFactory.getLogger(Jexlers.class);

    private final File dir;
    private final String id;

    /** key is jexler id */
    private final Map<String,Jexler> jexlerMap;
    private final List<Jexler> jexlers;

    private final List<Issue> issues;

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
                if (id != null && !jexlerMap.containsKey(id)) {
                    Jexler jexler = new Jexler(file, this);
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
    public void autostart() {
        for (Jexler jexler : jexlers) {
            if (jexler.isAutostart()) {
            	jexler.start();
            }
        }
    }

    @Override
    public void start() {
        for (Jexler jexler : jexlers) {
            jexler.start();
        }
    }

    public void waitForStartup(long timeout) {
        long t0 = System.currentTimeMillis();
        do {
        	boolean hasTimedOut = (System.currentTimeMillis() - t0 > timeout);
        	boolean isNoneBusyStarting = true;
        	for (Jexler jexler : jexlers) {
        		RunState runState = jexler.getRunState();
                if (runState == RunState.BUSY_STARTING) {
                	if (hasTimedOut) {
                		trackIssue(new Issue(jexler, "Timeout waiting for jexler startup", null));
                	}
                	isNoneBusyStarting = false;
                }
            }
        	if (isNoneBusyStarting || hasTimedOut) {
        		return;
        	}
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        } while (true);
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
    public void stop() {
        for (Jexler jexler : jexlers) {
            jexler.stop();
        }
    }

    public void waitForShutdown(long timeout) {
        long t0 = System.currentTimeMillis();
        do {
        	boolean hasTimedOut = (System.currentTimeMillis() - t0 > timeout);
        	boolean areAllOff = true;
        	for (Jexler jexler : jexlers) {
        		RunState runState = jexler.getRunState();
                if (runState != RunState.OFF) {
                	if (hasTimedOut) {
                		trackIssue(new Issue(jexler, "Timeout waiting for jexler shutdown", null));
                	}
                	areAllOff = false;
                }
            }
        	if (areAllOff || hasTimedOut) {
        		return;
        	}
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        } while (true);
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
     * Get jexlers, sorted by id.
     * @return jexlers
     */
    public List<Jexler> getJexlers() {
        return jexlers;
    }

    /**
     * Get jexler for given id.
     * @param id
     * @return jexler for given id or null if none
     */
    public Jexler getJexler(String id) {
        return jexlerMap.get(id);
    }

}
