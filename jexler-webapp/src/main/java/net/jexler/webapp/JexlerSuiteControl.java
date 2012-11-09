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

package net.jexler.webapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerSuite;

/**
 * Jexler suite control bean.
 *
 * @author $(whois jexler.net)
 */
public class JexlerSuiteControl {

    public JexlerSuiteControl() {
        // empty
    }

    public JexlerSuite getSuite() {
        return JexlerContextListener.getJexlerSuite();
    }

    public Map<String,JexlerControl> getJexlerControls() {
        List<Jexler> jexlers = getSuite().getJexlers();
        Map<String,JexlerControl> jexlerControls = new HashMap<String,JexlerControl>();
        for (Jexler jexler : jexlers) {
            jexlerControls.put(jexler.getId(), new JexlerControl(jexler));
        }
        return jexlerControls;
    }

    public String getStartStop() {
        boolean isAnyRunning = false;
        for (Jexler jexler : getSuite().getJexlers()) {
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
                if (cmd.equals("start")) {
                    suite.start();
                } else if (cmd.equals("stop")) {
                    suite.stop();
                } else if (cmd.equals("restart")) {
                    suite.stop();
                    suite.start();
                }
            } else {
                Jexler jexler = suite.getJexler(jexlerId);
                if (jexler != null) {
                    if (cmd.equals("start")) {
                        jexler.start();
                    } else if (cmd.equals("stop")) {
                        jexler.stop();
                    } else if (cmd.equals("restart")) {
                        jexler.stop();
                        jexler.start();
                    }
                }
            }
        }
        // LATER return success/error message?
        return "";
    }

}
