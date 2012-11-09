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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.jexler.core.Jexler;

/**
 * Jexler config file control.
 *
 * @author $(whois jexler.net)
 */
public class JexlerConfigFileControl {

    private final Jexler jexler;
    private final File configFile;

    public JexlerConfigFileControl(Jexler jexler, File configFile) {
        this.jexler = jexler;
        this.configFile = configFile;
    }

    public String getName() {
        return configFile.getName();
    }

    public String getNameLink() {
        // TODO urlencode name
        String name = configFile.getName();
        return "<a href='?cmd=info&jexler=" + jexler.getId() + "&file=" + name + "'>" + name + "</a>";
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();

        String[] split = configFile.getName().split("\\.");
        if (split.length < 2) {
            // TODO
            //log.error("Script file '{}' has no extension", scriptFile.getAbsolutePath());
        }
        String fileExtension = split[split.length-1];

        builder.append("<pre class='brush: " + fileExtension + "; gutter: false;'>\n");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            do {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.replace("<", "&lt;");
                builder.append(line);
                builder.append("\n");
            } while (true);
            reader.close();
        } catch (IOException e) {
            // TODO handle better, log
            return ("Error reading file '" + configFile.getAbsolutePath() + "'");
        }

        builder.append("</pre>");
        return builder.toString();
    }

}
