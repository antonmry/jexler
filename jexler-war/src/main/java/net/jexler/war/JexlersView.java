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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;

import net.jexler.Issue;
import net.jexler.Jexler;
import net.jexler.JexlerUtil;
import net.jexler.Jexlers;
import net.jexler.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jexler(s) view.
 *
 * TODO refactor/explain (mix of jexler and jexlers in one view)
 *
 * @author $(whois jexler.net)
 */
public class JexlersView {

    static final Logger log = LoggerFactory.getLogger(JexlersView.class);

    private final Jexlers jexlers;
    private final File logfile;
    private final long stopTimeout;

    private String jexlerId;
    private Jexler jexler;

    private HttpServletRequest request;
    private String targetJexlerId;
    private Jexler targetJexler;

    public JexlersView() {
        jexlers = JexlerContextListener.getJexlers();
        logfile = JexlerContextListener.getLogfile();
        stopTimeout = JexlerContextListener.getStopTimeout();
    }

    private void setJexler(Jexler jexler) {
        this.jexler = jexler;
        jexlerId = jexler.getId();
    }

    public String handleCommands(HttpServletRequest request) {
        this.request = request;
        jexlers.refresh();
        // set jexler from request parameter
        targetJexlerId = request.getParameter("jexler");
        if (targetJexlerId != null) {
            targetJexler = jexlers.getJexler(targetJexlerId);
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
                // TODO
            }
        }

        jexlers.refresh();

        // LATER return success/error message?
        return "";
    }

    public Map<String,JexlersView> getJexlers() {
        List<Jexler> jexlerList = jexlers.getJexlers();
        Map<String,JexlersView> jexlersViews = new LinkedHashMap<>();
        for (Jexler jexler : jexlerList) {
            JexlersView view = new JexlersView();
            view.setJexler(jexler);
            jexlersViews.put(jexler.getId(), view);
        }
        return jexlersViews;
    }

    public String getJexlerId() {
        return jexlerId;
    }

    public String getJexlerIdLink() {
        return "<a href='?cmd=info" + getJexlerParam() + "'>" + jexlerId + "</a>";
    }

    public String getStartStop() {
        boolean isRunning = false;
        if (jexlerId != null) {
            isRunning = jexler.isRunning();
        } else {
            for (Jexler jexler : jexlers.getJexlers()) {
                if (jexler.isRunning()) {
                    isRunning = true;
                    break;
                }
            }
        }
        if (isRunning) {
            return "<a href='?cmd=stop" + getJexlerParam() + "'><img src='stop.gif'></a>";
        } else {
            return "<a href='?cmd=start" + getJexlerParam() + "'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a href='?cmd=restart" + getJexlerParam() + "'><img src='restart.gif'></a>";
    }

    public String getLog() {
        // TODO handle better
        if (jexlerId == null) {
            if (jexlers.getIssues().size() == 0) {
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
            issues = jexlers.getIssues();
        } else {
            issues = jexler.getIssues();
        }
        if (issues.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<pre>");
        for (Issue issue : issues) {
            builder.append("\n");
            // LATER: get date format from logback?
            SimpleDateFormat format = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss.SSS");
            builder.append("<strong>Date:      </strong>" + format.format(issue.getDate()) + "\n");
            builder.append("<strong>Message:   </strong>" + issue.getMessage() + "\n");
            Service<?> service = issue.getService();
            String s;
            if (service == null) {
                s = "-";
            } else {
                s = service.getClass().getName() + ":" + service.getId();
            }
            builder.append("<strong>Service:   </strong>" + s + "\n");
            Exception e= issue.getException();
            s = (e==null) ? "-" : e.toString();
            builder.append("<strong>Exception: </strong>" + s.replace("<", "&lt;") + "\n");
            s = issue.getStackTrace();
            if (s != null) {
                // TODO css
                builder.append(s.isEmpty() ? "" : "<font color='gray' size='-3'>"+ s.replace("<", "&lt;") + "</font>\n");
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
        builder.append("<pre>\n\n");
        try {
            String logData = JexlerUtil.readTextFileReversedLines(logfile);
            logData = logData.replace("<", "&lt;");
            builder.append(logData);
        } catch (IOException e) {
            String msg = "Could not read logfile '" + logfile.getAbsolutePath() + "'.";
            jexlers.trackIssue(new Issue(null, msg, e));
            builder.append(msg);
        }
        builder.append("</pre>\n");
        String s = builder.toString();
        if (jexlerId != null) {
            s = s.replace(jexlerId, "<strong>" + jexlerId + "</strong>");
        }
        return s;
    }

    public String getSource() {
        if (jexler == null) {
            return "";
        }
        File file = jexler.getFile();
        try {
            return JexlerUtil.readTextFile(file);
        } catch (IOException e) {
            String msg = "Could not read jexler script file '" + file.getAbsolutePath() + "'.";
            jexler.trackIssue(new Issue(null, msg, e));
            return msg;
        }
    }

    public String getMimeType() {
        if (jexler == null) {
            return "text/plain";
        }
        MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        map.addMimeTypes("text/x-ruby rb");
        map.addMimeTypes("text/x-python py");
        map.addMimeTypes("text/x-groovy groovy");
        String mimeType = map.getContentType(jexler.getFile());
        return mimeType;
    }

    private void handleStart() {
        if (targetJexlerId == null) {
            jexlers.start();
        } else if (targetJexler != null) {
            targetJexler.start();
        }
    }

    private void handleStop() {
        if (targetJexlerId == null) {
            jexlers.stop(stopTimeout);
        } else if (targetJexler != null) {
            targetJexler.stop(stopTimeout);
        }
    }

    private void handleSaveAs() {
        String source = request.getParameter("source");
        if (source != null) {
            File file = new File(jexlers.getDir(), targetJexlerId);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(source);
            } catch (IOException e) {
                String msg = "Could not save script file '" + file.getAbsolutePath() + "'";
                if (targetJexler != null) {
                    targetJexler.trackIssue(new Issue(null, msg, e));
                } else {
                    jexlers.trackIssue(new Issue(null, msg, e));
                }
            }
        }
    }

    private void handleDelete() {
        File file = new File(jexlers.getDir(), targetJexlerId);
        file.delete();
    }

    private void handleForget() {
        if (targetJexler != null) {
            targetJexler.forgetIssues();
        } else {
            jexlers.forgetIssues();
        }
    }

    private void handleForgetAll() {
        jexlers.forgetIssues();
        for (Jexler jexler : jexlers.getJexlers()) {
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

}
