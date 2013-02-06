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

import net.jexler.core.Jexler;
import net.jexler.core.JexlerHandler;

/**
 * Jexler handler view.
 *
 * @author $(whois jexler.net)
 */
public class JexlerHandlerView {

    @SuppressWarnings("unused")
    private final Jexler jexler;
    private final JexlerHandler handler;

    public JexlerHandlerView(Jexler jexler, JexlerHandler handler) {
        this.jexler = jexler;
        this.handler = handler;
    }

    public String getClassName() {
        String justName = handler.getClass().getSimpleName();
        String name = handler.getClass().getName();
        String pre = name.substring(0, name.length() - justName.length());
        return "<span class='dim'>" + pre + "</span>" + justName;
    }

    public String getDescription() {
        return handler.getDescription();
    }

}