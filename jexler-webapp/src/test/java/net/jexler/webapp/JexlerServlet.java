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

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerHandler;

/**
 * Jexler servlet.
 *
 * @author $(whois jexler.net)
 */
public class JexlerServlet extends HttpServlet {

    private static final long serialVersionUID = 4739439669503477884L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>Welcome</title>");
        response.getWriter().println("<style type=\"text/css\">");
        response.getWriter().println("img {border:0px}");
        response.getWriter().println("</style>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("<a href=\"http://www.jexler.net/htt/\"><img src=\"jexler.jpg\"></a>");
        response.getWriter().println("<pre>");
        response.getWriter().println("");
        Jexler jexler = JexlerContextListener.getJexler();
        List<JexlerHandler> handlers = jexler.getHandlers();
        for (JexlerHandler handler : handlers) {
            response.getWriter().println("- " + handler.getClass().getName()
                    + ":" + handler.getId() + " -- " + handler.getDescription());
        }
        response.getWriter().println("</pre>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
        //response.getWriter().println("session=" + request.getSession(true).getId());
    }
}
