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

package net.jexler.war

import net.jexler.Issue
import net.jexler.Jexler
import net.jexler.JexlerContainer
import net.jexler.JexlerUtil
import net.jexler.service.ServiceState
import net.jexler.service.Service

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.jsp.PageContext
import java.text.SimpleDateFormat

/**
 * Jexler(s) view.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerContainerView {

    private static final Logger log = LoggerFactory.getLogger(JexlerContainerView.class)

    private final JexlerContainer container
    private final File logfile
    private final long startTimeout
    private final long stopTimeout

    private String jexlerId
    private Jexler jexler

    private PageContext pageContext
    private HttpServletRequest request
    private HttpServletResponse response
    private String targetJexlerId
    private Jexler targetJexler

    JexlerContainerView() {
        container = JexlerContextListener.container
        logfile = JexlerContextListener.logfile
        startTimeout = JexlerContextListener.startTimeout
        stopTimeout = JexlerContextListener.stopTimeout
    }

    private void setJexler(Jexler jexler) {
        this.jexler = jexler
        jexlerId = jexler.id
    }

    String handleCommands(PageContext pageContext) {
        this.pageContext = pageContext
        request = (HttpServletRequest)pageContext.request
        response = (HttpServletResponse)pageContext.response
        container.refresh()
        // set jexler from request parameter
        targetJexlerId = request.getParameter('jexler')
        if (targetJexlerId != null) {
            targetJexler = container.getJexler(targetJexlerId)
        }

        String cmd = request.getParameter('cmd')
        if (cmd != null) {
            switch (cmd) {
            case 'start':
                handleStart()
                break
            case 'stop':
                handleStop()
                break
            case 'restart':
                handleRestart()
                break
            case 'zap':
                handleZap()
                break
            case 'save':
                handleSaveAs()
                break
            case 'delete':
                handleDelete()
                break
            case 'forget':
                handleForget()
                break
            case 'forgetall':
                handleForgetAll()
                break
            case 'http':
                handleHttp()
                break
            default:
                // ignore
                break
            }
        }

        container.refresh()

        return ''
    }

    String getJexlerTooltip() {
        return JexlerContextListener.jexlerTooltip
    }
    
    Map<String,JexlerContainerView> getJexlers() {
        Map<String,JexlerContainerView> jexlersViews = new LinkedHashMap<>()
        for (Jexler jexler : container.jexlers) {
            JexlerContainerView view = new JexlerContainerView()
            view.jexler = jexler
            jexlersViews.put(jexler.id, view)
        }
        return jexlersViews
    }

    String getJexlerId() {
        return jexlerId
    }

    String getJexlerIdLink() {
        // italic if busy ("running")
        String id = jexlerId
        if (jexler.state.busy) {
            id = "<em>$id</em>"
        }
        return "<a href='?cmd=info$jexlerParam'>$id</a>"
    }

    String getStartStopZap() {
        boolean on
        boolean zap = false
        if (jexlerId == null) {
            on = container.state.on
        } else {
            on = jexler.state.on
            if (on) {
                for (Issue issue : jexler.issues) {
                    if (issue.getMessage() == 'Timeout waiting for jexler shutdown.') {
                        zap = true
                        break
                    }
                }
            }
        }
        if (on) {
            return zap ? getZap() : getStop()
        } else {
            return getStart()
        }
    }

    String getStart() {
        String title = jexlerId == null ? 'start all with autostart set' : 'start'
        return "<a href='?cmd=start$jexlerParam' title='$title'><img src='start.gif'></a>"
    }

    String getStop() {
        String title = jexlerId == null ? 'stop all' : 'stop'
        return "<a href='?cmd=stop$jexlerParam' title='$title'><img src='stop.gif'></a>"
    }

    String getZap() {
        String title = jexlerId == null ? 'zap all' : 'zap'
        return "<a href='?cmd=zap$jexlerParam' title='$title'><img src='zap.gif'></a>"
    }

    String getRestart() {
        String title = jexlerId == null ? 'stop all, then start all' : 'restart'
        return "<a href='?cmd=restart$jexlerParam' title='$title'><img src='restart.gif'></a>"
    }

    String getStateInfo() {
        return jexler.state.info
    }

    String getLog() {
         if (jexlerId == null) {
            if (container.issues.size() == 0) {
                return "<a href='?cmd=log$jexlerParam' title='show log'><img src='log.gif'></a>"
            } else {
                return "<a href='?cmd=log$jexlerParam' title='show log'><img src='error.gif'></a>"
            }
        } else {
            if (jexler.issues.size() == 0) {
                return "<img src='ok.gif' title='no issues'>"
            } else {
                String title = "show issues (${jexler.issues.size()})"
                return "<a href='?cmd=log$jexlerParam' title='$title'><img src='error.gif'></a>"
            }

        }
    }

    String getWeb() {
        boolean available = false
        if (jexlerId != null) {
            Script script = jexler.script
            if (script != null && jexler.state.operational) {
                MetaClass mc = script.metaClass
                Object[] args = [ PageContext.class ]
                MetaMethod mm = mc.getMetaMethod('handleHttp', args)
                if (mm != null) {
                    available = true
                }

            }
        }
        if (available) {
            return "<a href='?cmd=http$jexlerParam' title='web'><img src='web.gif'></a>"
        } else {
            return "<img src='space.gif'>"
        }
    }

    String getIssues() {
        List<Issue> issues
        if (jexler == null) {
            issues = container.issues
        } else {
            issues = jexler.issues
        }
        if (issues.size() == 0) {
            return''
        }

        StringBuilder builder = new StringBuilder()
        Map<String,String> replacements = getReplacements()
        builder.append("<pre class='issues'>")
        for (Issue issue : issues) {
            builder.append('\n')
            SimpleDateFormat format = new SimpleDateFormat('EEE dd MMM yyyy HH:mm:ss.SSS')
            builder.append("<strong>Date:      </strong>${format.format(issue.date)}\n")
            builder.append("<strong>Message:   </strong>$issue.message\n")
            Service service = issue.service
            String s
            if (service == null) {
                s = '-'
            } else {
                s = "${service.class.name}:$service.id"
            }
            builder.append("<strong>Service:   </strong>$s\n")
            Throwable cause = issue.cause
            s = (cause==null) ? "-" : cause.toString()
            replacements.each { original, replacement ->
                s = s.replace(original, replacement)
            }
            builder.append("<strong>Cause: </strong>$s\n")
            s = issue.stackTrace
            if (s != null) {
                replacements.each { original, replacement ->
                    s = s.replace(original, replacement)
                }
                builder.append(s.empty ?: "<span class='trace'>$s</span>\n")
            }
        }
        builder.append('</pre>')
        return builder.toString()
    }

    String getLogfile() {
        if (jexlerId != null) {
            return ''
        }
        StringBuilder builder = new StringBuilder()
        builder.append("<pre class='log'>\n")
        try {
            String logData = readTextFileReversedLines(logfile)
            logData = logData.replace('<', '&lt')
            builder.append(logData)
        } catch (IOException e) {
            String msg = "Could not read logfile '$logfile.absolutePath'."
            container.trackIssue(null, msg, e)
            builder.append(msg)
        }
        builder.append('</pre>\n')
        String s = builder.toString()
        return s
    }

    String getSource() {
        if (jexler == null) {
            return ''
        }
        File file = jexler.file
        try {
            return file.text
        } catch (IOException e) {
            String msg = "Could not read jexler script file '$file.absolutePath'."
            jexler.trackIssue(null, msg, e)
            return msg
        }
    }

    String getMimeType() {
        if (jexler == null) {
            return 'text/plain'
        }
        MimetypesFileTypeMap map = new MimetypesFileTypeMap()
        map.addMimeTypes('text/x-groovy groovy')
        String mimeType = map.getContentType(jexler.file)
        return mimeType
    }
    
    String getScriptAllowEdit() {
        return Boolean.toString(JexlerContextListener.scriptAllowEdit)
    }
    
    String getDisabledIfReadonly() {
        boolean allowEdit = JexlerContextListener.scriptAllowEdit
        if (allowEdit) {
            return ''
        } else {
            return " disabled='disabled'"
        }
    }

    boolean isConfirmSave() {
        return JexlerContextListener.scriptConfirmSave
    }

    boolean isConfirmDelete() {
        return JexlerContextListener.scriptConfirmDelete
    }

    private void handleStart() {
        if (targetJexlerId == null) {
            container.start()
            runInNewThread { JexlerUtil.waitForStartup(container, startTimeout) }
        } else if (targetJexler != null) {
            targetJexler.start()
            runInNewThread { JexlerUtil.waitForStartup(targetJexler, startTimeout) }
        }
    }

    private void handleStop() {
        if (targetJexlerId == null) {
            container.stop()
            runInNewThread { JexlerUtil.waitForShutdown(container, stopTimeout) }
        } else if (targetJexler != null) {
            targetJexler.stop()
            runInNewThread { JexlerUtil.waitForShutdown(targetJexler, stopTimeout) }
        }
    }

    private void handleRestart() {
        if (targetJexlerId == null) {
            container.stop()
            if (JexlerUtil.waitForShutdown(container, startTimeout)) {
                container.start()
                runInNewThread { JexlerUtil.waitForStartup(container, startTimeout) }
            }
        } else if (targetJexler != null) {
            targetJexler.stop()
            if (JexlerUtil.waitForShutdown(targetJexler, stopTimeout)) {
                targetJexler.start()
                runInNewThread { JexlerUtil.waitForStartup(targetJexler, startTimeout) }
            }
        }
    }

    private void handleZap() {
        if (targetJexlerId == null) {
            container.zap()
        } else if (targetJexler != null) {
            targetJexler.zap()
        }
    }

    private void handleSaveAs() {
        if (!JexlerContextListener.scriptAllowEdit) {
            return
        }
        String source = request.getParameter('source')
        if (source != null) {
            source = source.replace('\r\n', '\n')
            File file = container.getJexlerFile(targetJexlerId)
            try {
                file.text = source
            } catch (IOException e) {
                String msg = "Could not save script file '${file.absolutePath}'"
                if (targetJexler != null) {
                    targetJexler.trackIssue(null, msg, e)
                } else {
                    container.trackIssue(null, msg, e)
                }
            }
        }
    }

    private void handleDelete() {
        if (!JexlerContextListener.scriptAllowEdit) {
            return
        }
        File file = container.getJexlerFile(targetJexlerId)
        file.delete()
    }

    private void handleForget() {
        if (targetJexler != null) {
            targetJexler.forgetIssues()
        } else {
            container.forgetIssues()
        }
    }

    private void handleForgetAll() {
        container.forgetIssues()
        for (Jexler jexler : container.jexlers) {
            jexler.forgetIssues()
        }
    }

    private void handleHttp() {
        if (targetJexlerId == null) {
            sendError(response, 404, 'No jexler parameter indicated.', null)
            return
        }
        if (targetJexler == null) {
            sendError(response, 404, "Jexler '$targetJexlerId' not found.", null)
            return
        }

        Script script = targetJexler.getScript()
        if (script == null || !targetJexler.state.operational) {
            sendError(response, 404, "Jexler '$targetJexlerId' is not operational.", null)
            return
        }

        MetaClass mc = script.metaClass
        Object[] args = [ PageContext.class ]

        MetaMethod mm = mc.getMetaMethod('handleHttp', args)
        if (mm == null) {
            sendError(response, 404, "Jexler '$targetJexlerId' does not handle HTTP requests.", null)
            return
        }

        try {
            mm.invoke(script, [ pageContext ] as Object[])
        } catch (Throwable t) {
            targetJexler.trackIssue(jexler, "Handler 'handleHttp' failed.", t)
            sendError(response, 500, "Jexler '$targetJexlerId': Handler 'handleHttp' failed.", t)
        }
    }

    private void sendError(HttpServletResponse response, int status, String msg, Throwable t) {
        response.status = status
        String stacktrace = ''
        if (t != null) {
            Map<String,String> replacements = getReplacements()
            stacktrace = JexlerUtil.getStackTrace(t)
            replacements.each { original, replacement ->
                stacktrace = stacktrace.replace(original, replacement)
            }
            stacktrace = "<hr><pre class='log'>$stacktrace</pre><hr>"

        }
        response.writer.println("""\
<html>
  <head>
    <title>Error $status</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <link rel="shortcut icon" href="favicon.ico"/>
    <link rel="icon" href="favicon.ico"/>
    <link rel="stylesheet" href="jexler.css"/>
  </head>
  <body>
    <a href="."><img src="jexler.jpg" title="$jexlerTooltip}"></a>
    <h1><font color="red">Error $status</font></h1>
    <p>$msg</p>
    $stacktrace
  </body>
</html>
""")
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, 'UTF-8')
    }

    private String getJexlerParam() {
        if (jexlerId == null) {
            return ''
        } else {
            return "&jexler=${urlEncode(jexlerId)}"
        }
    }
    
    /**
     * Read log file into string while reversing the order of lines.
     * @param file
     * @return file contents (platform default charset and line separator)
     * @throws IOException if reading failed
     */
    private static String readTextFileReversedLines(File file) throws IOException {
        StringBuilder builder = new StringBuilder()
        file.eachLine { line ->
            builder.insert(0, line + System.lineSeparator())
        }
        return builder.toString()
    }

    private Map<String,String> getReplacements() {
        Map<String,String> replacements = new LinkedHashMap<String,String>()
        replacements.put('<', '&lt')
        replacements.put('Jexler.start', '<strong>Jexler.start</strong>')
        replacements.put('Jexler$1.run', '<strong>Jexler$1.run</strong>')
        for (Jexler jexler : container.jexlers) {
            String original = "${jexler.id}.groovy"
            String replacement = "<strong>$original</strong>"
            replacements.put(original, replacement)
        }
        return replacements
    }

    private void runInNewThread(Closure closure) {
        new Thread() {
            void run() {
                closure.call()
            }
        }.start()
    }

}
