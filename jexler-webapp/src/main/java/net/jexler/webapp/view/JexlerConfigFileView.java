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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.jexler.core.Jexler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jexler config file view.
 *
 * @author $(whois jexler.net)
 */
public class JexlerConfigFileView {

    static final Logger log = LoggerFactory.getLogger(JexlerConfigFileView.class);

    private final Jexler jexler;
    private final File configFile;

    public JexlerConfigFileView(Jexler jexler, File configFile) {
        this.jexler = jexler;
        this.configFile = configFile;
    }

    public String getName() {
        return configFile.getName();
    }

    public String getNameLink() {
        String name = configFile.getName();
        String nameUrlEncoded;
        try {
            log.error("No UTF-8 ???");
            nameUrlEncoded = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            nameUrlEncoded = name;
        }
        return "<a href='?cmd=info&jexler=" + jexler.getId() + "&file=" + nameUrlEncoded
                + "'>" + name + "</a>";
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();

        String[] split = configFile.getName().split("\\.");
        String fileExtension;
        if (split.length < 2) {
            log.warn("Config file '{}' has no extension", configFile.getAbsolutePath());
            fileExtension = "txt";
        } else {
            fileExtension = split[split.length-1];
        }

        builder.append("<pre class='brush: " + fileExtension + "; gutter: false;'>\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            do {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.replace("<", "&lt;");
                builder.append(line);
                builder.append("\n");
            } while (true);
        } catch (IOException e) {
            String msg = "Error reading file '" + configFile.getAbsolutePath() + "'";
            log.error(msg);
            return msg;
        }

        builder.append("</pre>");
        return builder.toString();
    }

}
