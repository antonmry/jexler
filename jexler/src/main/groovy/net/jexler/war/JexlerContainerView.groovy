/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

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
 * Jexler(s) view, used in Jexler.jsp.
 *
 * @author $(whois jexler.net)
 */
@CompileStatic
class JexlerContainerView {

    private static final Logger log = LoggerFactory.getLogger(Jexler.class)

    // Markers for when processing jexler stop/start/restart
    private static Set<String> processing = Collections.synchronizedSet([] as Set)

    // JSP/HTTP
    private PageContext pageContext
    private HttpServletRequest request
    private HttpServletResponse response

    // The jexler and jexler ID set after constructing
    // in getJexlers(), but not for "container" in JSP,
    // hence null if container, not null if a jexler
    private Jexler jexler
    private String jexlerId

    // The 'jexler' parameter,
    // set when handling request in handleCommands()
    private String targetJexlerId

    // The jexler for the 'jexler' parameter,
    // set when handling request in handleCommands()
    private Jexler targetJexler

    // Constructor
    JexlerContainerView() {
    }

    // Handle commands (based on request parameters)
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
            // redirect to self to allow reloading page in browser
            if (request.method == 'POST' && cmd != 'http') {
                response.sendRedirect(getAction(targetJexlerId))
            }
        }

        container.refresh()

        return ''
    }

    // Get all jexlers from container
    Map<String,JexlerContainerView> getJexlers() {
        final Map<String,JexlerContainerView> jexlersViews = new LinkedHashMap<>()
        for (Jexler jexler : container.jexlers) {
            JexlerContainerView view = new JexlerContainerView()
            view.jexler = jexler
            view.jexlerId = jexler.id
            jexlersViews.put(jexler.id, view)
        }
        return jexlersViews
    }

    // Get jexler ID, needed in JSP
    String getJexlerId() {
        return jexlerId
    }

    // Set jexler ID, needed to set in views for jexlers
    void setJexlerId(String jexlerId) {
        this.jexlerId = jexlerId
    }

    // Get tooltip for jexler webapp
    String getJexlerTooltip() {
        return JexlerContextListener.jexlerTooltip
    }

    // Get start/stop link with icon for table of jexlers
    String getStartStop() {
        boolean on
        if (jexlerId == null) {
            on = container.state.on
        } else {
            on = jexler.state.on
        }
        return on ? getStop() : getStart()
    }

    // Get restart/zap link with icon for table of jexlers
    String getRestartZap() {
        if (jexlerId != null && jexler.state.on) {
            for (Issue issue : jexler.issues) {
                if (issue.getMessage() == JexlerUtil.SHUTDOWN_TIMEOUT_MSG) {
                    return getZap()
                }
            }
        }
        return getRestart()
    }

    // Get form action
    private String getAction(String jexlerId) {
        return jexlerId != null ? "?jexler=$jexlerId" : '.'
    }

    // Get link for posting form for start/stop/restart/zap buttons
    private String getLink(String cmdParam, String imgName, String imgTitle) {
        String type = cmdParam == null ? 'button' : 'submit'
        return """\
            <form action="${getAction(jexlerId)}" method="post">\
            <button class="img" type="$type" name="cmd" value="$cmdParam">\
            <img src="${imgName}.gif"${imgTitle == null ? '' : " title='$imgTitle'"}>\
            </button>\
            </form>""".replace('            ', '')
    }

    // Get start link with icon for table of jexlers
    String getStart() {
        final String title = jexlerId == null ? 'start all with autostart set' : 'start'
        String cmd = isProcessing(jexlerId) ? null : 'start'
        return getLink(cmd, 'start', title)
    }

    // Get stop link with icon for table of jexlers
    String getStop() {
        final String title = jexlerId == null ? 'stop all' : 'stop'
        String cmd = isProcessing(jexlerId) ? null : 'stop'
        return getLink(cmd, 'stop', title)
     }

    // Get restart link with icon for table of jexlers
    String getRestart() {
        final String title = jexlerId == null ? 'stop all, then start all with autostart set' : 'restart'
        String cmd = isProcessing(jexlerId) ? null : 'restart'
        return getLink(cmd, 'restart', title)
    }

    // Get zap link with icon for table of jexlers
    String getZap() {
        final String title = jexlerId == null ? 'zap all (unsafe)' : 'zap (unsafe)'
        String cmd = isProcessing(jexlerId) ? null : 'zap'
        return getLink(cmd, 'zap', title)
    }

    // Get link to jexler for table of jexlers
    String getJexlerIdLink() {
        // italic if busy ("running")
        String id = jexlerId
        if (jexler.state.busy) {
            id = "<em>$id</em>"
        }
        return "<a href='?cmd=info$jexlerParam' title='${jexler.state.info}'>$id</a>"
    }

    // Get web link and icon for table of jexlers
    String getWeb() {
        if (jexlerId == null) {
            return "<a href='https://www.jexler.net/'><img src='space.gif'></a>"
        }
        Script script = jexler.script
        if (script != null && jexler.state.operational) {
            MetaClass mc = script.metaClass
            Object[] args = [PageContext.class]
            MetaMethod mm = mc.getMetaMethod('handleHttp', args)
            if (mm != null) {
                return "<a href='?cmd=http$jexlerParam' title='web'><img src='web.gif'></a>"
            }

        }
        return "<img src='space.gif'>"
    }

    // Get link and icon for logfile and/or issues
    String getLog() {
        if (jexlerId == null) {
            if (container.issues.size() == 0) {
                return "<a href='?cmd=log$jexlerParam' title='show log'><img src='log.gif'></a>"
            } else {
                return "<a href='?cmd=log$jexlerParam' title='show log'><img src='error.gif'></a>"
            }
        } else {
            final String title = jexler.issues.empty ? 'no issues' : "show issues (${jexler.issues.size()})"
            String imgPart = jexler.issues.empty ? "img src='ok.gif'" : "img src='error.gif'"
            if (isProcessing(jexlerId)) {
                imgPart = "img src='wheel.gif' class='wheel'"
            }
            if (jexler.issues.size() == 0) {
                return "<$imgPart title='$title'>"
            } else {
                return "<a href='?cmd=log$jexlerParam' title='$title'><$imgPart></a>"
            }
        }
    }

    // Get issues for jexler or container
    String getIssues() {
        final List<Issue> issues
        if (jexler == null) {
            issues = container.issues
        } else {
            issues = jexler.issues
        }
        if (issues.size() == 0) {
            return''
        }

        final StringBuilder builder = new StringBuilder()
        builder.append("<pre class='issues'>")
        for (Issue issue : issues) {
            builder.append('\n')
            SimpleDateFormat format = new SimpleDateFormat('EEE dd MMM yyyy HH:mm:ss.SSS')
            builder.append("<strong>Date:      </strong>${format.format(issue.date)}\n")
            builder.append("<strong>Message:   </strong>$issue.message\n")
            final Service service = issue.service
            String s
            if (service == null) {
                s = '-'
            } else {
                s = "${service.class.name}:$service.id"
            }
            builder.append("<strong>Service:   </strong>$s\n")
            final Throwable cause = issue.cause
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

    // Get logfile
    String getLogfile() {
        if (jexlerId != null) {
            return ''
        }
        final File logfile = JexlerContextListener.logfile
        final StringBuilder builder = new StringBuilder()
        builder.append("<pre class='log'>\n")
        try {
            String logData = readTextFileReversedLines(logfile)
            logData = logData.replace('<', '&lt')
            builder.append(logData)
        } catch (IOException e) {
            final String msg = "Could not read logfile '$logfile.absolutePath'."
            container.trackIssue(null, msg, e)
            builder.append(msg)
        }
        builder.append('</pre>\n')
        final String s = builder.toString()
        return s
    }

    // Get script source
    String getSource() {
        if (jexler == null) {
            return ''
        }
        final File file = jexler.file
        if (!file.exists()) {
            return ''
        }
        try {
            return file.text
        } catch (IOException e) {
            String msg = "Could not read jexler script file '$file.absolutePath'."
            jexler.trackIssue(null, msg, e)
            return msg
        }
    }

    // Get mime type from file extension
    String getMimeType() {
        if (jexler == null) {
            return 'text/plain'
        }
        final MimetypesFileTypeMap map = new MimetypesFileTypeMap()
        map.addMimeTypes('text/x-groovy groovy')
        final String mimeType = map.getContentType(jexler.file)
        return mimeType
    }

    // Whether editing scripts is allowed by config or not
    String getScriptAllowEdit() {
        return Boolean.toString(JexlerContextListener.scriptAllowEdit)
    }

    // Return disabled attribute if not allowed to edit scripts
    String getDisabledIfReadonly() {
        final boolean allowEdit = JexlerContextListener.scriptAllowEdit
        if (allowEdit) {
            return ''
        } else {
            return " disabled='disabled'"
        }
    }

    // Whether to ask before saving scripts or not according to config
    boolean isConfirmSave() {
        return JexlerContextListener.scriptConfirmSave
    }

    // Whether to ask before deleting scripts or not according to config
    boolean isConfirmDelete() {
        return JexlerContextListener.scriptConfirmDelete
    }

    // Start jexler or container
    private void handleStart() {
        if (targetJexlerId == null) {
            for (Jexler jexler : container.jexlers) {
                if (jexler.metaInfo.autostart) {
                    handleStart(jexler)
                }
            }
        } else {
            handleStart(targetJexler)
        }
    }

    // Stop jexler or container
    private void handleStop() {
        if (targetJexlerId == null) {
            for (Jexler jexler : container.jexlers) {
                if (jexler.state.on) {
                    handleStop(jexler)
                }
            }
        } else {
            handleStop(targetJexler)
        }
    }

    // Restart jexler or container
    private void handleRestart() {
        if (targetJexlerId == null) {
            for (Jexler jexler : container.jexlers) {
                handleRestart(jexler)
            }
        } else {
            handleRestart(targetJexler)
        }
    }

    // Zap jexler or container
    private void handleZap() {
        if (targetJexlerId == null) {
            for (Jexler jexler : container.jexlers) {
                if (jexler.state.on) {
                    handleZap(jexler)
                }
            }
        } else {
            handleZap(targetJexler)
        }
    }

    // Save script
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

    // Delete script
    private void handleDelete() {
        if (!JexlerContextListener.scriptAllowEdit) {
            return
        }
        final File file = container.getJexlerFile(targetJexlerId)
        file.delete()
    }

    // Forget issues of jexler or container
    private void handleForget() {
        if (targetJexler != null) {
            targetJexler.forgetIssues()
        } else {
            container.forgetIssues()
        }
    }

    // Forget issues of container and of all jexlers
    private void handleForgetAll() {
        container.forgetIssues()
        for (Jexler jexler : container.jexlers) {
            jexler.forgetIssues()
        }
    }

    // Handle HTTP event, dispatch to jexler or reply directly in case of errors
    private void handleHttp() {
        if (targetJexlerId == null) {
            sendError(response, 404, 'No jexler parameter indicated.', null)
            return
        }
        if (targetJexler == null) {
            sendError(response, 404, "Jexler '$targetJexlerId' not found.", null)
            return
        }

        final Script script = targetJexler.getScript()
        if (script == null || !targetJexler.state.operational) {
            sendError(response, 404, "Jexler '$targetJexlerId' is not operational.", null)
            return
        }

        final MetaClass mc = script.metaClass
        final Object[] args = [ PageContext.class ]

        final MetaMethod mm = mc.getMetaMethod('handleHttp', args)
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

    // Send error page
    private void sendError(HttpServletResponse response, int status, String msg, Throwable t) {
        response.status = status
        String stacktrace = ''
        if (t != null) {
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

    // Get one and only container in this webapp
    private static JexlerContainer getContainer() {
        return JexlerContextListener.container
    }

    // Value of the 'jexler' parameter, either jexler ID or empty for container
    private String getJexlerParam() {
        if (jexlerId == null) {
            return ''
        } else {
            return "&jexler=${urlEncode(jexlerId)}"
        }
    }

    // Encode URL (UTF-8 character encoding)
    private static String urlEncode(String s) {
        return URLEncoder.encode(s, 'UTF-8')
    }

    /**
     * Read log file into string while reversing the order of lines.
     * @param file
     * @return file contents (platform default charset and line separator)
     * @throws IOException if reading failed
     */
    private static String readTextFileReversedLines(File file) throws IOException {
        final StringBuilder builder = new StringBuilder()
        file.eachLine { line ->
            builder.insert(0, line + System.lineSeparator())
        }
        return builder.toString()
    }

    // Get map of passages to highlight in stack trace
    private Map<String,String> getReplacements() {
        final Map<String,String> replacements = new LinkedHashMap<String,String>()
        replacements.put('<', '&lt')
        // start jexler script
        replacements.put('Jexler.start', '<strong>Jexler.start</strong>')
        // jexler script thread
        replacements.put('Jexler$1.run', '<strong>Jexler$1.run</strong>')
        // thread that zaps jexler script thread
        replacements.put('Jexler$2.run', '<strong>Jexler$2.run</strong>')
        // individual jexlers
        for (Jexler jexler : container.jexlers) {
            String original = "${jexler.id}.groovy"
            String replacement = "<strong>$original</strong>"
            replacements.put(original, replacement)
        }
        return replacements
    }

    // Run given closure in a new thread
    private void runInNewThread(Closure closure) {
        new Thread() {
            void run() {
                closure.call()
            }
        }.start()
    }

    // Whether is processing given jexler / container
    private static boolean isProcessing(String jexlerId) {
        if (jexlerId == null) {
            // always allow to stop/start all, must be robust
            return false
        } else {
            return processing.contains(jexlerId)
        }
    }

    // Start given jexler
    private void handleStart(Jexler jexler) {
        if (processing.contains(jexler.id)) {
            return
        }
        processing.add(jexler.id)
        jexler.start()
        runInNewThread {
            JexlerUtil.waitForStartup(jexler, JexlerContextListener.startTimeout)
            processing.remove(jexler.id)
        }
    }

    // Stop given jexler
    private void handleStop(Jexler jexler) {
        if (processing.contains(jexler.id)) {
            return
        }
        processing.add(jexler.id)
        jexler.stop()
        runInNewThread {
            JexlerUtil.waitForShutdown(jexler, JexlerContextListener.stopTimeout)
            processing.remove(jexler.id)
        }
    }

    // Restart given jexler
    private void handleRestart(Jexler jexler) {
        if (processing.contains(jexler.id)) {
            return
        }
        processing.add(jexler.id)
        jexler.stop()
        runInNewThread {
            if (JexlerUtil.waitForShutdown(jexler, JexlerContextListener.stopTimeout)) {
                processing.remove(jexler.id)
                handleStart(jexler)
            } else {
                processing.remove(jexler.id)
            }
        }
    }

    // Zap given jexler
    private void handleZap(Jexler jexler) {
        processing.add(jexler.id)
        jexler.zap()
        processing.remove(jexler.id)
    }

}
