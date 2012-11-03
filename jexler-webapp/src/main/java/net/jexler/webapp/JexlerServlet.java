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
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerSuite;

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
        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Jexler</title>");

        // LATER export style to separate file?
        writer.println("<style type='text/css'>");
        writer.println("img {border:0px}");
        writer.println("table {");
        writer.println("border-width: 1px 1px 1px 1px;");
        writer.println("border-spacing: 1px;");
        writer.println("border-style: solid;");
        writer.println("}");
        writer.println("td {");
        writer.println("border-width: 1px 1px 1px 1px;");
        writer.println("padding: 2px;");
        writer.println("border-style: solid;");
        writer.println("}");
        writer.println("span.dim {color:gray}");
        writer.println("div.ok {color:green}");
        writer.println("</style>");

        writer.println("</head>");
        writer.println("<body>");
        writer.println("<a href='jexler'><img src='jexler.jpg'></a>");

        JexlerSuite jexlerSuite = JexlerContextListener.getJexlerSuite();
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<td><strong>ID</strong></td>");
        writer.println("<td><strong>Description</strong></td>");
        writer.println("</tr>");
        List<Jexler> jexlers = jexlerSuite.getJexlers();
        for (Jexler jexler : jexlers) {
            writer.println("<tr>");
            writer.println("<td>" + jexler.getId() + "</td>");
            writer.println("<td>" + jexler.getDescription() + "</td>");
            writer.println("</tr>");
        }
        writer.println("</table>");

        /*
        String method = request.getParameter("method");
        if (method != null) {
            if (method.equals("startup")) {
                jexler.startup();
            } else if (method.equals("shutdown")) {
                jexler.shutdown();
            } else if (method.equals("restart")) {
                jexler.shutdown();
                jexler.startup();
            }
        }

        writer.println("<form action='jexler' method='get'>");
        //writer.println("<input type='hidden' value='startup'>");
        writer.println("<input type='submit' name='method' value='startup'"
                + (!jexler.isRunning() ? "" : " disabled='disabled'")
                + ">");
        writer.println("<input type='submit' name='method' value='shutdown'"
                + (jexler.isRunning() ? "" : " disabled='disabled'")
                + ">");
        writer.println("<input type='submit' name='method' value='restart'>");
        writer.println("</form>");

        if (jexler.isRunning()) {
            writer.println("<table>");
            writer.println("<tr>");
            writer.println("<td><strong>Handler Class</strong></td>");
            writer.println("<td><strong>ID</strong></td>");
            writer.println("<td><strong>Description</strong></td>");
            writer.println("</tr>");
            List<JexlerHandler> handlers = jexler.getHandlers();
            for (JexlerHandler handler : handlers) {
                writer.println("<tr>");
                String fullName = handler.getClass().getName();
                String simpleName = handler.getClass().getSimpleName();
                String classPath = fullName.substring(0, fullName.length() - simpleName.length());
                writer.println("<td><span class='dim'>"
                        + classPath + "</span>" + simpleName + "</td>");
                writer.println("<td>" + handler.getId() + "</td>");
                writer.println("<td>" + handler.getDescription() + "</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");
        }

        if (method != null) {
            // LATER handle case of unknown method?
            writer.println("<div class='ok'>" + method + " ok</div>");
        }
        */

        writer.println("</body>");
        writer.println("</html>");
    }
}
