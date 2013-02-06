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

package net.jexler.webapp.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerSuite;
import net.jexler.webapp.JexlerContextListener;

/**
 * Jexler suite view.
 *
 * @author $(whois jexler.net)
 */
public class JexlerSuiteView {

    private final JexlerSuite suite;

    public JexlerSuiteView() {
        suite = JexlerContextListener.getJexlerSuite();
    }

    public Map<String,JexlerView> getJexlers() {
        List<Jexler> jexlers = suite.getJexlers();
        Map<String,JexlerView> jexlerViews = new LinkedHashMap<>();
        for (Jexler jexler : jexlers) {
            jexlerViews.put(jexler.getId(), new JexlerView(suite, jexler));
        }
        return jexlerViews;
    }

    public String getStartStop() {
        boolean isAnyRunning = false;
        for (Jexler jexler : suite.getJexlers()) {
            if (jexler.isRunning()) {
                isAnyRunning = true;
                break;
            }
        }
        if (isAnyRunning) {
            return "<a class='stop' href='?cmd=stop'><img src='stop.gif'></a>";
        } else {
            return "<a class='start' href='?cmd=start'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a class='restart' href='?cmd=restart'><img src='restart.gif'></a>";
    }

    public String handleCommands(HttpServletRequest request) {
        String cmd = request.getParameter("cmd");
        if (cmd != null) {
            String jexlerId = request.getParameter("jexler");
            JexlerSuite suite = JexlerContextListener.getJexlerSuite();
            if (jexlerId == null) {
                switch (cmd) {
                case "start":
                    suite.start();
                    break;
                case "stop":
                    suite.stop();
                    break;
                case "restart":
                    suite.stop();
                    suite.start();
                    break;
                default:
                    // TODO
                }
            } else {
                Jexler jexler = suite.getJexler(jexlerId);
                if (jexler != null) {
                    switch (cmd) {
                    case "start":
                        jexler.start();
                        break;
                    case "stop":
                        jexler.stop();
                        break;
                    case "restart":
                        jexler.stop();
                        jexler.start();
                        break;
                    default:
                        // TODO
                    }
                }
            }
        }
        // LATER return success/error message?
        return "";
    }

}
