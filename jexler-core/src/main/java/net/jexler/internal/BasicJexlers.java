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

package net.jexler.internal;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.jexler.Issue;
import net.jexler.IssueTracker;
import net.jexler.Jexler;
import net.jexler.JexlerFactory;
import net.jexler.Jexlers;
import net.jexler.RunState;
import net.jexler.service.BasicServiceGroup;
import net.jexler.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic default implementation of jexlers interface.
 *
 * @author $(whois jexler.net)
 */
public class BasicJexlers extends BasicServiceGroup implements Jexlers {

    private static final Logger log = LoggerFactory.getLogger(BasicJexlers.class);
	
	private static final String EXT = ".groovy";

    private final File dir;
    private final String id;
    private final JexlerFactory jexlerFactory;

    /** key is jexler id */
    private final Map<String,Jexler> jexlerMap;

    private final IssueTracker issueTracker;

    /**
     * Constructor.
     * @param dir directory which contains jexler scripts
     * @param jexlerFactory factory for creating jexler instances
     * @throws RuntimeException if given dir is not a directory or does not exist
     */
    public BasicJexlers(File dir, JexlerFactory jexlerFactory) {
    	super(dir.exists() ? dir.getName() : null);
        if (!dir.exists()) {
            throw new RuntimeException("Directory '" + dir.getAbsolutePath() + "' does not exist.");
        } else  if (!dir.isDirectory()) {
            throw new RuntimeException("File '" + dir.getAbsolutePath() + "' is not a directory.");
        }
        this.dir = dir;
        id = super.getId();
        this.jexlerFactory = jexlerFactory;
        jexlerMap = new TreeMap<>();
        issueTracker = new BasicIssueTracker();
        refresh();
    }

    /**
     * Refresh list of jexlers.
     * Add new jexlers for new script files;
     * remove old jexlers if their script file is gone and they are stopped.
     */
    @Override
    public void refresh() {
        synchronized (jexlerMap) {
            // list directory and create jexlers in map for new script files in directory
            File[] files = dir.listFiles();
            files = files != null ? files : new File[0];
            for (File file : files) {
                if (file.isFile() && !file.isHidden()) {
                    String id = getJexlerId(file);
                    if (id != null && !jexlerMap.containsKey(id)) {
                        Jexler jexler = jexlerFactory.get(file, this);
                        jexlerMap.put(jexler.getId(), jexler);
                    }
                }
            }

            // recreate list while omitting jexlers without script file that are stopped
            List<Jexler> jexlers = (List<Jexler>)(List<?>)getServices();
            jexlers.clear();
            for (String id : jexlerMap.keySet()) {
                Jexler jexler = jexlerMap.get(id);
                if (jexler.getFile().exists() || jexler.isOn()) {
                    jexlers.add(jexler);
                }
            }

            // recreate map with list entries
            jexlerMap.clear();
            for (Jexler jexler : jexlers) {
                jexlerMap.put(jexler.getId(), jexler);
            }
        }
    }

    @Override
    public void autostart() {
        for (Jexler jexler : getJexlers()) {
            if (jexler.getMetaInfo().isOn("autostart", false)) {
            	jexler.start();
            }
        }
    }

    @Override
    public boolean waitForStartup(long timeout) {
    	boolean ok = super.waitForStartup(timeout);
    	if (!ok) {
    		for (Jexler jexler : getJexlers()) {
                if (jexler.getRunState() == RunState.BUSY_STARTING) {
                	trackIssue(jexler, "Timeout waiting for jexler startup.", null);
                }
            }
    	}
    	return ok;
    }
    
    @Override
    public boolean waitForShutdown(long timeout) {
    	boolean ok = super.waitForShutdown(timeout);
    	if (!ok) {
    		for (Jexler jexler : getJexlers()) {
                if (jexler.getRunState() != RunState.OFF) {
                	trackIssue(jexler, "Timeout waiting for jexler shutdown.", null);
                }
            }
    	}
    	return ok;
    }

    @Override
    public void trackIssue(Issue issue) {
        issueTracker.trackIssue(issue);
    }

    @Override
    public void trackIssue(Service service, String message, Throwable cause) {
    	issueTracker.trackIssue(service, message, cause);
    }

    @Override
    public List<Issue> getIssues() {
        return issueTracker.getIssues();
    }

    @Override
    public void forgetIssues() {
    	issueTracker.forgetIssues();
    }

    /**
     * Get id, which is the name of the jexlers directory.
     */
    @Override
    public String getId() {
        return id;
    }

    public File getDir() {
        return dir;
    }

	@Override
    @SuppressWarnings("unchecked")
    public List<Jexler> getJexlers() {
        List<Jexler> jexlers = new LinkedList<>();
        synchronized(jexlerMap) {
            jexlers.addAll((List<Jexler>)(List<?>)getServices());
        }
    	return Collections.unmodifiableList(jexlers);
    }

    @Override
    public Jexler getJexler(String id) {
        synchronized(jexlerMap) {
            return jexlerMap.get(id);
        }
    }

	@Override
	public File getJexlerFile(String id) {
    	return new File(dir, id + EXT);
	}

	@Override
	public String getJexlerId(File jexlerFile) {
		String name = jexlerFile.getName();
		if (name.endsWith(EXT)) {
			return name.substring(0, name.length() - EXT.length());
		} else {
			return null;
		}
	}

}
