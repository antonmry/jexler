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

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.text.SimpleDateFormat

import javax.activation.MimetypesFileTypeMap
import javax.servlet.http.HttpServletRequest

import net.jexler.Issue
import net.jexler.Jexler
import net.jexler.JexlerContainer
import net.jexler.RunState
import net.jexler.service.Service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    private HttpServletRequest request
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

    String handleCommands(HttpServletRequest request) {
        this.request = request
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
                handleStop()
                handleStart()
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
            default:
                // ignore
                break
            }
        }

        container.refresh()

        return ''
    }

    String getVersion() {
        return JexlerContextListener.version
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
        RunState runState = jexler.runState
        boolean isBusy = (runState == RunState.BUSY_STARTING
                || runState == RunState.BUSY_EVENT
                || runState == RunState.BUSY_STOPPING)
        String id = jexlerId
        if (isBusy) {
            id = "<em>$id</em>"
        }
        return "<a href='?cmd=info$jexlerParam'>$id</a>"
    }

    String getStartStop() {
        boolean on
        if (jexlerId == null) {
            on = container.on
        } else {
            on = jexler.on
        }
        if (on) {
            return "<a href='?cmd=stop$jexlerParam'><img src='stop.gif'></a>"
        } else {
            return "<a href='?cmd=start$jexlerParam'><img src='start.gif'></a>"
        }
    }

    String getRestart() {
        return "<a href='?cmd=restart$jexlerParam'><img src='restart.gif'></a>"
    }
    
    String getRunStateInfo() {
        return jexler.runState.info
    }

    String getLog() {
         if (jexlerId == null) {
            if (container.issues.size() == 0) {
                return "<a href='?cmd=log$jexlerParam'><img src='log.gif'></a>"
            } else {
                return "<a href='?cmd=log$jexlerParam'><img src='error.gif'></a>"
            }
        } else {
            if (jexler.issues.size() == 0) {
                return "<a href='?cmd=log$jexlerParam'><img src='ok.gif'></a>"
            } else {
                return "<a href='?cmd=log$jexlerParam'><img src='error.gif'></a>"
            }

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
            builder.append("<strong>Cause: </strong>${s.replace('<', '&lt')}\n")
            s = issue.stackTrace
            if (s != null) {
                builder.append(s.empty ?: "<span class='trace'>${s.replace('<', '&lt')}</span>\n")
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
            return new String(Files.readAllBytes(file.toPath()))
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
        return Boolean.toString(JexlerContextListener.scriptAllowEdit())
    }
    
    String getDisabledIfReadonly() {
        boolean allowEdit = JexlerContextListener.scriptAllowEdit()
        if (allowEdit) {
            return ''
        } else {
            return " disabled='disabled'"
        }
    }

    boolean isConfirmSave() {
        return JexlerContextListener.scriptConfirmSave()
    }

    boolean isConfirmDelete() {
        return JexlerContextListener.scriptConfirmDelete()
    }

    private void handleStart() {
        if (targetJexlerId == null) {
            container.start()
            container.waitForStartup(startTimeout)
        } else if (targetJexler != null) {
            targetJexler.start()
            targetJexler.waitForStartup(startTimeout)
        }
    }

    private void handleStop() {
        if (targetJexlerId == null) {
            container.stop()
            container.waitForShutdown(stopTimeout)
        } else if (targetJexler != null) {
            targetJexler.stop()
            targetJexler.waitForShutdown(stopTimeout)
        }
    }

    private void handleSaveAs() {
        if (!JexlerContextListener.scriptAllowEdit()) {
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
        if (!JexlerContextListener.scriptAllowEdit()) {
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


}
