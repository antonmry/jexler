<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>

<jsp:useBean id="container" class="net.jexler.war.JexlerContainerView">
<%= container.handleCommands(pageContext) %>
<c:set var="jexler" value="${container.jexlers[param.jexler]}"/>
<c:set var="jexler" value="${jexler != null ? jexler : container }"/>

<c:if test="${param.cmd != 'http'}">

<c:if test="${param.cmd != 'status'}">

  <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

  <htmL>

  <head>

  <title>Jexler</title>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
  <link rel="shortcut icon" href="favicon.ico"/>
  <link rel="icon" href="favicon.ico"/>
  <link rel="stylesheet" href="cm/lib/codemirror.css">
  <link rel="stylesheet" href="jexler.css"/>
  <script src="cm/lib/codemirror.js"></script>
  <script src="cm/addon/edit/matchbrackets.js"></script>
  <script src="cm/mode/groovy/groovy.js"></script>
  <style type="text/css">.CodeMirror { border: 1px solid #eee; height: auto; }</style>

  <script>
    var savedSource;
    var currentSource;
    var hasSourceChanged;
    var hasJexlerChanged;
    var isGetStatusPending;
    var isLogGetStatus;

    function onPageLoad() {
      sourceElement = document.getElementById('source');
      if (sourceElement != null) {
        savedSource = sourceElement.value
      }
      currentSource = savedSource;
      hasSourceChanged = false;
      hasJexlerChanged = false;
      setHeight();
      isGetStatusPending = false;
      isLogGetStatus = false;
      window.setInterval(getStatus, 1000);
    }
        
    var previousStatusText = "";

    function getStatus() {
      if (isGetStatusPending) {
        logGetStatus('skipping')
        return;
      }
      var xmlhttp = new XMLHttpRequest();
      xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState === 4) {
          try {
            logGetStatus('=> readyState 4');
            var text = xmlhttp.responseText;
            if (xmlhttp.status / 100 != 2) {
              text = ""
            }
            if (text == "") {
              text = previousStatusText;
              if (text.indexOf("(offline)") < 0) {
                text = text.replace("<strong>Name</strong>", "<strong>(offline)</strong>");
                text = text.replace(/\.gif'/g, "-dim.gif'");
                text = text.replace(/<a href='\?cmd=[a-z]+(&jexler=[A-Za-z0-9]+)?'>/g, "");
                text = text.replace(/<\/a>/g, "");
                text = text.replace(/status-name/g, "status-name status-offline");
              }
            }
            if (text != previousStatusText) {
              previousStatusText = text;
              var statusDiv = document.getElementById("statusdiv");
              statusDiv.innerHTML = text;
            }
          } finally {
            logGetStatus('=> finally');
            isGetStatusPending = false;
          }
        }
      };
      xmlhttp.onabort = function() {
        logGetStatus('=> aborted');
        isGetStatusPending = false;
      }
      xmlhttp.onerror = function() {
        logGetStatus('=> error');
        isGetStatusPending = false;
      }
      xmlhttp.onload = function() {
        logGetStatus('=> loaded');
        isGetStatusPending = false;
      }
      xmlhttp.ontimeout = function() {
        logGetStatus('=> timeout');
        isGetStatusPending = false; }
      xmlhttp.open('GET', '?cmd=status', true);
      xmlhttp.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
      xmlhttp.timeout = 5000;
      logGetStatus('pending...');
      isGetStatusPending = true;
      xmlhttp.send(null);
    }

    function logGetStatus(info) {
      if (isLogGetStatus) {
        console.log(info);
      }
    }

    function updateSaveIndicator() {
      currentSource = editor.getValue();
      hasSourceChanged = (savedSource != currentSource);
      hasJexlerChanged = '${jexler.jexlerId}' != document.getElementById('newjexlername').value;
      if (hasJexlerChanged) {
        document.getElementById('savestatus').setAttribute("src", "ok.gif")
      } else if (hasSourceChanged) {
        document.getElementById('savestatus').setAttribute("src", "log.gif")
      } else {
        document.getElementById('savestatus').setAttribute("src", "white.gif")
      }
    }

    function isPostSave() {
      if (${container.confirmSave}) {
        if (!confirm("Are you sure you want to save '${jexler.jexlerId}'?")) {
          return false;
        }
      }
      if (hasJexlerChanged) {
        return true;
      }
      var xmlhttp = new XMLHttpRequest();
      xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState === 4) {
          var text = xmlhttp.responseText;
          if (xmlhttp.status / 100 == 2 && xmlhttp.responseText != "") {
            editor.focus();
            savedSource = currentSource;
            hasSourceChanged = false;
            document.getElementById('savestatus').setAttribute("src", "white.gif")
          }
        }
      };
      xmlhttp.open('POST', '?cmd=save&jexler=${jexler.jexlerId}', true);
      xmlhttp.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
      xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded; charset=utf-8");
      xmlhttp.timeout = 5000;
      xmlhttp.send("source=" + encodeURIComponent(currentSource));
      return false;
    }

    function isPostDelete() {
      if (${container.confirmDelete}) {
        return confirm("Are you sure you want to delete '${jexler.jexlerId}'?");
      } else {
        return true;
      }
    }

    window.onresize = function() {
      setHeight();
    }

    function setHeight() {
      var hTotal = document.documentElement.clientHeight;
      var hHeader = document.getElementById('header').offsetHeight;
      var h = hTotal - hHeader - 50;
      document.getElementById('sourcediv').style.height = "" + h + "px";
      document.getElementById('statusdiv').style.height = "" + h + "px";
    }
  </script>

  </head>

  <body onLoad="onPageLoad()">

  <div class="hidden">
    <script>
      new Image().src = "ok-dim.gif"
      new Image().src = "error-dim.gif"
      new Image().src = "log-dim.gif"
      new Image().src = "start-dim.gif"
      new Image().src = "stop-dim.gif"
      new Image().src = "restart-dim.gif"
      new Image().src = "web-dim.gif"
      new Image().src = "white-dim.gif"
    </script>
  </div>

  <form action="request.contextPath" method="post">
  
  <table class="frame">
  <tr id="header">
  <td class="frame">
  <a href="."><img src="jexler.jpg" title="${container.version}"></a>
  </td>
  <td class="frame frame-buttons">
    <c:choose>
    <c:when test="${param.cmd == 'log' || param.cmd == 'clearissues'}">
      <button type="submit" name="cmd" value="forget">Forget</button>
      <button type="submit" name="cmd" value="forgetall">Forget All</button>
      <input type="hidden" name="jexler" value="${jexler.jexlerId}">
    </c:when>
    <c:otherwise>
      <table class="frame">
      <tr>
        <td><button type="submit" name="cmd" value="save" ${container.disabledIfReadonly} onclick="return isPostSave()">Save as...</button></td>
        <td><button type="submit" name="cmd" value="delete" ${container.disabledIfReadonly} onclick="return isPostDelete()">Delete...</button></td>
        <td><input id="newjexlername" type="text" name="jexler" onkeyup="updateSaveIndicator()" value="${jexler.jexlerId}" ${container.disabledIfReadonly}></td>
        <td><img id="savestatus" src="white.gif"></td>
      </tr>
      </table>
    </c:otherwise>
    </c:choose>
  </td>
  </tr>
  <tr>
  <td class="frame">
  
  <div id="statusdiv" class="autoscroll">
</c:if>

<table class="status" id="status">

<tr>
<td class="status">${container.startStop}</td>
<td class="status">${container.restart}</td>
<td class="status">${container.log}</td>
<td class="status status-name"><strong>Name</strong></td>
<td class="status">${container.web}</td>
</tr>

<c:forEach items="${container.jexlers}" var="loopJexler">
  <tr>
  <td class="status">${loopJexler.value.startStop}</td>
  <td class="status">${loopJexler.value.restart}</td>
  <td class="status">${loopJexler.value.log}</td>
  <td class="status status-name" title="${loopJexler.value.runStateInfo}">${loopJexler.value.jexlerIdLink}</td>
  <td class="status">${loopJexler.value.web}</td>
  </tr>
</c:forEach>

</table>

<c:if test="${param.cmd != 'status'}">

  </div>
  
  </td>
  <td class="frame frame-text">

  <div id="sourcediv" class="autoscroll">
  <c:choose>
    <c:when test="${param.cmd == 'log' || param.cmd == 'clearissues'}">
      ${jexler.issues}
      ${jexler.logfile}
    </c:when>
    <c:otherwise>
      <textarea id="source" name="source">${jexler.source}</textarea>
      <script>
        var editor = CodeMirror.fromTextArea(document.getElementById("source"), {
          lineNumbers: true,
          mode: "${jexler.mimeType}",
          tabMode: "indent",
          matchBrackets: true,
          indentUnit: 2,
          readOnly: ${!container.scriptAllowEdit}
        });
        editor.on("change", function(cm, change) {
          updateSaveIndicator();
        });
      </script>
    </c:otherwise>
  </c:choose>
  </div>
  
  </td>
  </tr>
  </table>
  
  </form>

  </body>

  </html>

</c:if>

</c:if>

</jsp:useBean>
