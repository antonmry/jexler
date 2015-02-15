<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.*" %>
<%@ page import="net.jexler.war.*" %>
<%@ page trimDirectiveWhitespaces="true" %>

<jsp:useBean id="jexlers" class="net.jexler.war.JexlersView">

<c:if test="${param.cmd != 'status'}">

  <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

  <htmL>

  <head>

  <title>Jexler</title>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
  <link rel="shortcut icon" href="favicon.ico"/>
  <link rel="icon" href="favicon.ico"/>
  <link rel="stylesheet" href="jexler.css"/>

  <script src="cm/lib/codemirror.js"></script>
  <link rel="stylesheet" href="cm/lib/codemirror.css">
  <script src="cm/addon/edit/matchbrackets.js"></script>
  <script src="cm/mode/groovy/groovy.js"></script>
  <style type="text/css">.CodeMirror { border: 1px solid #eee; height: auto; } </style>

  <script>
    function onPageLoad() {
      window.setInterval(getStatus, 3000);
      setHeight();
    }
        
    var previousText = "";

    function getStatus() {
      var xmlhttp = new XMLHttpRequest();
      xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState === 4) {
          var text = xmlhttp.responseText;
          if (xmlhttp.status / 100 != 2) {
            text = ""
          }
          if (text == "") {
            text = previousText;
            if (text.indexOf("(offline)") < 0) {
              text = text.replace("<strong>Name</strong>", "<strong>(offline)</strong>");
              text = text.replace(/\.gif'/g, "-dim.gif'");
              text = text.replace(/<a href='\?cmd=[a-z]+(&jexler=[A-Za-z0-9]+)?'>/g, "");
              text = text.replace(/<\/a>/g, "");
              text = text.replace(/status-name/g, "status-name status-offline");
            }
          }
          if (text != previousText) {
            previousText = text;
            var statusDiv = document.getElementById("statusdiv");
            statusDiv.innerHTML = text;
          }
        }
      };
      xmlhttp.open('GET', '?cmd=status', true);
      xmlhttp.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
      xmlhttp.timeout = 5000;
      xmlhttp.send(null);
    }

    window.onresize = function() {
      setHeight();
    }

    function setHeight() {
      var hTotal = document.documentElement.clientHeight;        
      var hHeader = document.getElementById('header').offsetHeight;
      var h = hTotal - hHeader - 50;
      document.getElementById('sourcediv').style.height="" + h + "px";
      document.getElementById('statusdiv').style.height="" + h + "px";
    }
  </script>

  </head>

  <body onLoad="onPageLoad()">
  
  <%= jexlers.handleCommands(request) %>
  <c:set var="jexler" value="${jexlers.jexlers[param.jexler]}"/>
  <c:set var="jexler" value="${jexler != null ? jexler : jexlers }"/>
  
  <div class="hidden">
    <script>
      new Image().src = "ok-dim.gif"
      new Image().src = "error-dim.gif"
      new Image().src = "log-dim.gif"
      new Image().src = "start-dim.gif"
      new Image().src = "stop-dim.gif"
      new Image().src = "restart-dim.gif"
    </script>
  </div>

  <form action="request.contextPath" method="post">
  
  <table class="frame">
  <tr id="header">
  <td class="frame">
  <a href="."><img src="jexler.jpg" title="${jexlers.version}"></a>
  </td>
  <td class="frame frame-buttons">
    <c:choose>
    <c:when test="${param.cmd == 'log' || param.cmd == 'clearissues'}">
      <p></p>
        <button type="submit" name="cmd" value="forget">Forget</button>
        <button type="submit" name="cmd" value="forgetall">Forget All</button>
        <input type="hidden" name="jexler" value="${jexler.jexlerId}">
    </c:when>
    <c:otherwise>
      <p></p>
        <button type="submit" name="cmd" value="save" ${jexlers.disabledIfReadonly} ${jexlers.scriptConfirmSave}>Save as...</button>
        <button type="submit" name="cmd" value="delete" ${jexlers.disabledIfReadonly} ${jexlers.scriptConfirmDelete}>Delete...</button>
        <input type="text" name="jexler" value="${jexler.jexlerId}" ${jexlers.disabledIfReadonly}>
    </c:otherwise>
    </c:choose>
  </td>
  </tr>
  <tr>
  <td class="frame">
  
  <div id="statusdiv" style="overflow-y: auto">
</c:if>

<table class="status" id="status">

<tr>
<td class="status">${jexlers.startStop}</td>
<td class="status">${jexlers.restart}</td>
<td class="status">${jexlers.log}</td>
<td class="status status-name"><strong>Name</strong></td>
</tr>

<c:forEach items="${jexlers.jexlers}" var="loopJexler">
  <tr>
  <td class="status">${loopJexler.value.startStop}</td>
  <td class="status">${loopJexler.value.restart}</td>
  <td class="status">${loopJexler.value.log}</td>
  <td class="status status-name" title="${loopJexler.value.runStateInfo}">${loopJexler.value.jexlerIdLink}</td>
  </tr>
</c:forEach>

</table>

<c:if test="${param.cmd != 'status'}">

  </div>
  
  </td>
  <td class="frame frame-text">

  <div id="sourcediv" style="overflow-y: auto">
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
          readOnly: ${!jexlers.scriptAllowEdit}
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

</jsp:useBean>
