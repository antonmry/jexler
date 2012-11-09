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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerHandler;

/**
 * Jexler control.
 *
 * @author $(whois jexler.net)
 */
public class JexlerControl {

    private final Jexler jexler;

    public JexlerControl(Jexler jexler) {
        this.jexler = jexler;
    }

    public Jexler getJexler() {
        return jexler;
    }

    public String getId() {
        return jexler.getId();
    }

    public String getIdLink() {
        String id = jexler.getId();
        return "<a href='?jexler=" + id + "&cmd=info'>" + id + "</a>";
    }

    public String getDescription() {
        return jexler.getDescription();
    }

    public Map<String,JexlerHandlerControl> getHandlers() {
        List<JexlerHandler> handlers = jexler.getHandlers();
        Map<String,JexlerHandlerControl> handlerControls = new HashMap<String,JexlerHandlerControl>();
        for (JexlerHandler handler : handlers) {
            handlerControls.put(handler.getId(), new JexlerHandlerControl(handler));
        }
        return handlerControls;
    }

    public Map<String,JexlerConfigFileControl> getConfigFiles() {
        File dir = jexler.getDir();
        File[] allFiles = dir.listFiles();
        Map<String,JexlerConfigFileControl> configControls = new HashMap<String,JexlerConfigFileControl>();
        for (File file : allFiles) {
            if (file.isFile() && !file.isHidden()) {
                configControls.put(file.getName(), new JexlerConfigFileControl(jexler, file));
            }
        }
        return configControls;
    }

    public String getStartStop() {
        String id = jexler.getId();
        if (jexler.isRunning()) {
           return "<a class='stop' href='?jexler=" + id + "&cmd=stop'><img src='stop.gif'></a>";
        } else {
            return "<a class='start' href='?jexler=" + id + "&cmd=start'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        String id = jexler.getId();
        return "<a class='restart' href='?jexler=" + id + "&cmd=restart'><img src='restart.gif'></a>";
    }

}
