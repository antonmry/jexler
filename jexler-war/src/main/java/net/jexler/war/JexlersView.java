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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jexler.Jexler;
import net.jexler.JexlerUtil;
import net.jexler.Jexlers;

/**
 * Jexlers view.
 *
 * @author $(whois jexler.net)
 */
public class JexlersView {

    static final Logger log = LoggerFactory.getLogger(JexlersView.class);

    private final Jexlers jexlers;
    private final File logfile;
    private final long stopTimeout;

    public JexlersView() {
        jexlers = JexlerContextListener.getJexlers();
        jexlers.refresh();
        logfile = JexlerContextListener.getLogfile();
        stopTimeout = JexlerContextListener.getStopTimeout();
    }

    public Map<String,JexlerView> getJexlers() {
        List<Jexler> jexlerList = jexlers.getJexlers();
        Map<String,JexlerView> jexlerViews = new LinkedHashMap<>();
        for (Jexler jexler : jexlerList) {
            jexlerViews.put(jexler.getId(), new JexlerView(jexlers, jexler));
        }
        return jexlerViews;
    }

    public String getStartStop() {
        boolean isAnyRunning = false;
        for (Jexler jexler : jexlers.getJexlers()) {
            if (jexler.isRunning()) {
                isAnyRunning = true;
                break;
            }
        }
        if (isAnyRunning) {
            return "<a href='?cmd=stop'><img src='stop.gif'></a>";
        } else {
            return "<a href='?cmd=start'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a href='?cmd=restart'><img src='restart.gif'></a>";
    }

    public String getLog() {
        return "<a href='?cmd=log'><img src='log.gif'></a>";
    }

    public String getLogData() {
        try {
            String logData = JexlerUtil.readTextFileReversedLines(logfile);
            logData = logData.replace("<", "&lt;");
            return logData;
        } catch (IOException e) {
            String msg = "Error reading logfile '" + logfile.getAbsolutePath() + "'";
            log.error(msg, e);
            return msg;
        }
    }

    public String handleCommands(HttpServletRequest request) {
        String cmd = request.getParameter("cmd");
        if (cmd != null) {
            String jexlerId = request.getParameter("jexler");
            Jexlers jexlers = JexlerContextListener.getJexlers();
            if (jexlerId == null) {
                switch (cmd) {
                case "start":
                    jexlers.start();
                    break;
                case "stop":
                    jexlers.stop(stopTimeout);
                    break;
                case "restart":
                    jexlers.stop(stopTimeout);
                    jexlers.start();
                    break;
                default:
                    // TODO
                }
            } else {
                Jexler jexler = jexlers.getJexler(jexlerId);
                switch (cmd) {
                case "start":
                    if (jexler != null) {
                        jexler.start();
                    }
                    break;
                case "stop":
                    if (jexler != null) {
                        jexler.stop(stopTimeout);
                    }
                    break;
                case "restart":
                    if (jexler != null) {
                        jexler.stop(stopTimeout);
                        jexler.start();
                    }
                    break;
                case "save":
                    handleSaveAs(request, jexlerId);
                    break;
                case "delete":
                    handleDelete(request, jexlerId);
                    break;
                default:
                    // TODO
                }
            }
        }

        jexlers.refresh();

        // LATER return success/error message?
        return "";
    }

    private void handleSaveAs(HttpServletRequest request, String jexlerId) {
        String source = request.getParameter("source");
        if (source != null) {
            File file = new File(jexlers.getDir(), jexlerId);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(source);
            } catch (IOException e) {
                // LATER handle
            }
        }
    }

    private void handleDelete(HttpServletRequest request, String jexlerId) {
        File file = new File(jexlers.getDir(), jexlerId);
        file.delete();
    }

}
