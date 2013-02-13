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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.jexler.Jexler;
import net.jexler.Jexlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jexler view.
 *
 * @author $(whois jexler.net)
 */
public class JexlerView {

    static final Logger log = LoggerFactory.getLogger(JexlerView.class);

    @SuppressWarnings("unused")
    private final Jexlers jexlers;
    private final Jexler jexler;
    private final String name;
    private final String nameUrlEncoded;

    public JexlerView(Jexlers jexlers, Jexler jexler) {
        this.jexlers = jexlers;
        this.jexler = jexler;
        name = jexler.getName();
        try {
            nameUrlEncoded = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // practically never happens, Java must support UTF-8
            throw new RuntimeException(e);
        }

    }

    public String getName() {
        return name;
    }

    public String getNameLink() {
        return "<a href='?jexler=" + nameUrlEncoded + "&cmd=info'>" + name + "</a>";
    }

    public String getStartStop() {
        if (jexler.isRunning()) {
            return "<a class='stop' href='?jexler=" + nameUrlEncoded + "&cmd=stop'><img src='stop.gif'></a>";
        } else {
            return "<a class='start' href='?jexler=" + nameUrlEncoded + "&cmd=start'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a class='restart' href='?jexler=" + nameUrlEncoded + "&cmd=restart'><img src='restart.gif'></a>";
    }

    public String getFileText() {
        StringBuilder builder = new StringBuilder();
        File file = jexler.getFile();

        String[] split = file.getName().split("\\.");
        String fileExtension;
        if (split.length < 2) {
            log.warn("File '{}' has no extension", file.getAbsolutePath());
            fileExtension = "txt";
        } else {
            fileExtension = split[split.length-1];
        }

        builder.append("<pre class='brush: " + fileExtension + "; gutter: false;'>\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
            String msg = "Error reading file '" + file.getAbsolutePath() + "'";
            log.error(msg);
            return msg;
        }

        builder.append("</pre>");
        return builder.toString();
    }

}
