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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.activation.MimetypesFileTypeMap;

import net.jexler.Jexler;
import net.jexler.JexlerUtil;
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
    private final String id;
    private final String idUrlEncoded;

    public JexlerView(Jexlers jexlers, Jexler jexler) {
        this.jexlers = jexlers;
        this.jexler = jexler;
        id = jexler.getId();
        try {
            idUrlEncoded = URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // practically never happens, Java must support UTF-8
            throw new RuntimeException(e);
        }

    }

    public String getId() {
        return id;
    }

    public String getIdLink() {
        return "<a href='?jexler=" + idUrlEncoded + "&cmd=info'>" + id + "</a>";
    }

    public String getStartStop() {
        if (jexler.isRunning()) {
            return "<a href='?jexler=" + idUrlEncoded + "&cmd=stop'><img src='stop.gif'></a>";
        } else {
            return "<a href='?jexler=" + idUrlEncoded + "&cmd=start'><img src='start.gif'></a>";
        }
    }

    public String getRestart() {
        return "<a href='?jexler=" + idUrlEncoded + "&cmd=restart'><img src='restart.gif'></a>";
    }

    public String getLog() {
        // TODO ok or error?
        return "<a href='?jexler=" + idUrlEncoded + "&cmd=log'><img src='ok.gif'></a>";
    }

    public String getSource() {
        File file = jexler.getFile();
        try {
            return JexlerUtil.readTextFile(file);
        } catch (IOException e) {
            String msg = "Error reading file '" + file.getAbsolutePath() + "'";
            log.error(msg, e);
            return msg;
        }
    }

    public String getMimeType() {
        MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        map.addMimeTypes("text/x-ruby rb");
        map.addMimeTypes("text/x-python py");
        map.addMimeTypes("text/x-groovy groovy");
        String mimeType = map.getContentType(jexler.getFile());
        return mimeType;
    }

}
