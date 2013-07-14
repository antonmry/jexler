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
import java.nio.file.Files;
import java.nio.file.Path;
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

	static final Logger log = LoggerFactory.getLogger(BasicJexlers.class);
	
	static final String EXT = ".groovy";

    private final Path path;
    private final String id;
    private final JexlerFactory jexlerFactory;

    /** key is jexler id */
    private final Map<String,Jexler> jexlerMap;

    private final IssueTracker issueTracker;

    /**
     * Constructor.
     * @param path path for directory which contains jexler scripts
     */
    public BasicJexlers(Path path, JexlerFactory jexlerFactory) {
    	super(Files.exists(path) ? path.toFile().getName() : null);
        if (!Files.exists(path)) {
            throw new RuntimeException("Directory '" + path.toUri() + "' does not exist");
        } else  if (!Files.isDirectory(path)) {
            throw new RuntimeException("File '" + path.toUri() + "' is not a directory");
        }
        this.path = path;
        id = super.getId();
        this.jexlerFactory = jexlerFactory;
        jexlerMap = new TreeMap<String,Jexler>();
        issueTracker = new BasicIssueTracker();
        refresh();
    }

    /**
     * Refresh list of jexlers.
     * Add new jexlers for new script files.
     * Remove old jexlers if their script file is gone and they are stopped.
     */
    @Override
    public void refresh() {

        // list directory and create jexlers in map for new script files in directory
        File[] files = path.toFile().listFiles();
        for (File file : files) {
            if (file.isFile() && !file.isHidden()) {
                String id = getJexlerId(file.toPath());
                if (id != null && !jexlerMap.containsKey(id)) {
                    Jexler jexler = jexlerFactory.get(file.toPath(), this);
                    jexlerMap.put(jexler.getId(), jexler);
                }
            }
        }
        
        // recreate list while omitting jexlers without script file that are stopped
        getJexlers().clear();
        for (String id : jexlerMap.keySet()) {
            Jexler jexler = jexlerMap.get(id);
            if (Files.exists(jexler.getPath()) || jexler.isOn()) {
            	getJexlers().add(jexler);
            }
        }

        // recreate map with list entries
        jexlerMap.clear();
        for (Jexler jexler : getJexlers()) {
            jexlerMap.put(jexler.getId(), jexler);
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
                	trackIssue(jexler, "Timeout waiting for jexler startup", null);
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
                	trackIssue(jexler, "Timeout waiting for jexler shutdown", null);
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
    public void trackIssue(Service service, String message, Exception exception) {
    	issueTracker.trackIssue(service, message, exception);
    }

    @Override
    public List<Issue> getIssues() {
        return issueTracker.getIssues();
    }

    @Override
    public void forgetIssues() {
    	issueTracker.forgetIssues();
    }

    @Override
    public String getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }

	@Override
    @SuppressWarnings("unchecked")
    public List<Jexler> getJexlers() {
    	return (List<Jexler>)(List<?>)getServiceList();
    }

    @Override
    public Jexler getJexler(String id) {
        return jexlerMap.get(id);
    }

	@Override
	public Path getJexlerPath(String id) {
    	return new File(path.toFile(), id + EXT).toPath();
	}

	@Override
	public String getJexlerId(Path jexlerPath) {
		String name = jexlerPath.toFile().getName();
		if (name.endsWith(EXT)) {
			return name.substring(0, name.length() - EXT.length());
		} else {
			return null;
		}
	}

}
