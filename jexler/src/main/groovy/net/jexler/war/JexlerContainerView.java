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

package net.jexler.war;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;

import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.JexlerContainer;
import net.jexler.RunState;
import net.jexler.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jexler(s) view.
 *
 * @author $(whois jexler.net)
 */
public class JexlerContainerView {

	static final Logger log = LoggerFactory.getLogger(JexlerContainerView.class);

    private final JexlerContainer container;
    private final File logfile;
    private final long startTimeout;
    private final long stopTimeout;

    private String jexlerId;
    private Jexler jexler;

    private HttpServletRequest request;
    private String targetJexlerId;
    private Jexler targetJexler;

    public JexlerContainerView() {
        container = JexlerContextListener.getContainer();
        logfile = JexlerContextListener.getLogfile();
        startTimeout = JexlerContextListener.getStartTimeout();
        stopTimeout = JexlerContextListener.getStopTimeout();
    }

    private void setJexler(Jexler jexler) {
        this.jexler = jexler;
        jexlerId = jexler.getId();
    }

    public String handleCommands(HttpServletRequest request) {
        this.request = request;
        container.refresh();
        // set jexler from request parameter
        targetJexlerId = request.getParameter("jexler");
        if (targetJexlerId != null) {
            targetJexler = container.getJexler(targetJexlerId);
        }

        String cmd = request.getParameter("cmd");
        if (cmd != null) {
            switch (cmd) {
            case "start":
                handleStart();
                break;
            case "stop":
                handleStop();
                break;
            case "restart":
                handleStop();
                handleStart();
                break;
            case "save":
                handleSaveAs();
                break;
            case "delete":
                handleDelete();
                break;
            case "forget":
                handleForget();
                break;
            case "forgetall":
                handleForgetAll();
                break;
            default:
                // ignore
            }
        }

        container.refresh();

        return "";
    }

    public String getVersion() {
    	return JexlerContextListener.getVersion();
    }
    
    public Map<String,JexlerContainerView> getJexlers() {
        List<Jexler> jexlerList = container.getJexlers();
        Map<String,JexlerContainerView> jexlersViews = new LinkedHashMap<>();
        for (Jexler jexler : jexlerList) {
            JexlerContainerView view = new JexlerContainerView();
            view.setJexler(jexler);
            jexlersViews.put(jexler.getId(), view);
        }
        return jexlersViews;
    }

    public String getJexlerId() {
        return jexlerId;
    }

    public String getJexlerIdLink() {
    	// italic if busy ("running")
    	RunState runState = jexler.getRunState();
    	boolean isBusy = (runState == RunState.BUSY_STARTING
    			|| runState == RunState.BUSY_EVENT
    			|| runState == RunState.BUSY_STOPPING);
    	String id = jexlerId;
    	if (isBusy) {
    		id = "<em>" + id + "</em>";
    	}
        return "<a href='?cmd=info" + getJexlerParam() + "'>" + id + "</a>";
    }

    public String getStartStop() {
        boolean isOn;
        if (jexlerId == null) {
        	isOn = container.isOn();
        } else {
        	isOn = jexler.isOn();
        }
        if (isOn) {
            return "<a href='?cmd=stop" + getJexlerParam() + "'><img src='stop.gif'></a>";
        } else {
            return "<a href='?cmd=start" + getJexlerParam() + "'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a href='?cmd=restart" + getJexlerParam() + "'><img src='restart.gif'></a>";
    }
    
    public String getRunStateInfo() {
    	return jexler.getRunState().getInfo();
    }

    public String getLog() {
         if (jexlerId == null) {
            if (container.getIssues().size() == 0) {
                return "<a href='?cmd=log" + getJexlerParam() + "'><img src='log.gif'></a>";
            } else {
                return "<a href='?cmd=log" + getJexlerParam() + "'><img src='error.gif'></a>";
            }
        } else {
            if (jexler.getIssues().size() == 0) {
                return "<a href='?cmd=log" + getJexlerParam() + "'><img src='ok.gif'></a>";
            } else {
                return "<a href='?cmd=log" + getJexlerParam() + "'><img src='error.gif'></a>";
            }

        }
    }

    public String getIssues() {
        List<Issue> issues;
        if (jexler == null) {
            issues = container.getIssues();
        } else {
            issues = jexler.getIssues();
        }
        if (issues.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<pre class='issues'>");
        for (Issue issue : issues) {
            builder.append("\n");
            SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss.SSS");
            builder.append("<strong>Date:      </strong>" + format.format(issue.getDate()) + "\n");
            builder.append("<strong>Message:   </strong>" + issue.getMessage() + "\n");
            Service service = issue.getService();
            String s;
            if (service == null) {
                s = "-";
            } else {
                s = service.getClass().getName() + ":" + service.getId();
            }
            builder.append("<strong>Service:   </strong>" + s + "\n");
            Throwable cause = issue.getCause();
            s = (cause==null) ? "-" : cause.toString();
            builder.append("<strong>Cause: </strong>" + s.replace("<", "&lt;") + "\n");
            s = issue.getStackTrace();
            if (s != null) {
                builder.append(s.isEmpty() ? "" : "<span class='trace'>"+ s.replace("<", "&lt;") + "</span>\n");
            }
        }
        builder.append("</pre>");
        return builder.toString();
    }

    public String getLogfile() {
        if (jexlerId != null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<pre class='log'>\n");
        try {
            String logData = readTextFileReversedLines(logfile);
            logData = logData.replace("<", "&lt;");
            builder.append(logData);
        } catch (IOException e) {
            String msg = "Could not read logfile '" + logfile.getAbsolutePath() + "'.";
            container.trackIssue(null, msg, e);
            builder.append(msg);
        }
        builder.append("</pre>\n");
        String s = builder.toString();
        return s;
    }

    public String getSource() {
        if (jexler == null) {
            return "";
        }
        File file = jexler.getFile();
        try {
    		return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            String msg = "Could not read jexler script file '" + file.getAbsolutePath() + "'.";
            jexler.trackIssue(null, msg, e);
            return msg;
        }
    }

    public String getMimeType() {
        if (jexler == null) {
            return "text/plain";
        }
        MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        map.addMimeTypes("text/x-groovy groovy");
        String mimeType = map.getContentType(jexler.getFile());
        return mimeType;
    }
    
    public String getScriptAllowEdit() {
    	return Boolean.toString(JexlerContextListener.scriptAllowEdit());
    }
    
    public String getDisabledIfReadonly() {
    	boolean allowEdit = JexlerContextListener.scriptAllowEdit();
    	if (allowEdit) {
    		return "";
    	} else {
    		return " disabled='disabled'";
    	}
    }

    public boolean isConfirmSave() {
        return JexlerContextListener.scriptConfirmSave();
    }

    public boolean isConfirmDelete() {
        return JexlerContextListener.scriptConfirmDelete();
    }

    private void handleStart() {
        if (targetJexlerId == null) {
            container.start();
            container.waitForStartup(startTimeout);
        } else if (targetJexler != null) {
            targetJexler.start();
            targetJexler.waitForStartup(startTimeout);
        }
    }

    private void handleStop() {
        if (targetJexlerId == null) {
            container.stop();
            container.waitForShutdown(stopTimeout);
        } else if (targetJexler != null) {
            targetJexler.stop();
            targetJexler.waitForShutdown(stopTimeout);
        }
    }

    private void handleSaveAs() {
    	if (!JexlerContextListener.scriptAllowEdit()) {
    		return;
    	}
        String source = request.getParameter("source");
        if (source != null) {
            source = source.replace("\r\n", "\n");
            File file = container.getJexlerFile(targetJexlerId);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(source);
            } catch (IOException e) {
                String msg = "Could not save script file '" + file.getAbsolutePath() + "'";
                if (targetJexler != null) {
                    targetJexler.trackIssue(null, msg, e);
                } else {
                    container.trackIssue(null, msg, e);
                }
            }
        }
    }

    private void handleDelete() {
    	if (!JexlerContextListener.scriptAllowEdit()) {
    		return;
    	}
        File file = container.getJexlerFile(targetJexlerId);
        file.delete();
    }

    private void handleForget() {
        if (targetJexler != null) {
            targetJexler.forgetIssues();
        } else {
            container.forgetIssues();
        }
    }

    private void handleForgetAll() {
        container.forgetIssues();
        List<Jexler> jexlersList = container.getJexlers();
        for (Jexler jexler : jexlersList) {
            jexler.forgetIssues();
        }
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // practically never happens, Java must support UTF-8
            throw new RuntimeException(e);
        }
    }

    private String getJexlerParam() {
        if (jexlerId == null) {
            return "";
        } else {
            return "&jexler=" + urlEncode(jexlerId);
        }
    }
    
    /**
     * Read log file into string while reversing the order of lines.
     * @param file
     * @return file contents (platform default charset and line separator)
     * @throws IOException if reading failed
     */
    private static String readTextFileReversedLines(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.insert(0, line + System.lineSeparator());
            }
        }
        return builder.toString();
    }


}
