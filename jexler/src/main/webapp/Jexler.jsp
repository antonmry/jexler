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
      window.setInterval(getStatus, 1000);
    }
    
    var previousText = "";

    function getStatus() {
      var xmlhttp = new XMLHttpRequest();
      xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState === 4) {
          var text = xmlhttp.responseText;
          if (text != previousText) {
            previousText = text;
            var table = document.getElementById("status");
            table.innerHTML = text;
          }
        }
      };
      xmlhttp.open('GET', '?cmd=status', true);
      xmlhttp.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
      xmlhttp.send(null);
    }
  </script>

  </head>

  <body onLoad="onPageLoad()">

  <a href="."><img src="jexler.jpg" title="${jexlers.version}"></a>

  <%= jexlers.handleCommands(request) %>

  <table id="status">
</c:if>

<tr>
<td>${jexlers.startStop}</td>
<td>${jexlers.restart}</td>
<td>${jexlers.log}</td>
<td><strong>Name</strong></td>
</tr>

<c:forEach items="${jexlers.jexlers}" var="jexler">
  <tr>
  <td>${jexler.value.startStop}</td>
  <td>${jexler.value.restart}</td>
  <td>${jexler.value.log}</td>
  <td title="${jexler.value.runStateInfo}">${jexler.value.jexlerIdLink}</td>
  </tr>
</c:forEach>

<c:if test="${param.cmd != 'status'}">

  </table>

  <c:set var="jexler" value="${jexlers.jexlers[param.jexler]}"/>
  <c:set var="jexler" value="${jexler != null ? jexler : jexlers }"/>

  <c:choose>
    <c:when test="${param.cmd == 'log' || param.cmd == 'clearissues'}">
      <p></p>
      <form action="request.contextPath" method="post">
        <button type="submit" name="cmd" value="forget">Forget</button>
        <button type="submit" name="cmd" value="forgetall">Forget All</button>
        <input type="hidden" name="jexler" value="${jexler.jexlerId}">
        ${jexler.issues}
        <p></p>
        ${jexler.logfile}
      </form>
    </c:when>
    <c:otherwise>
      <p></p>
      <form action="request.contextPath" method="post">
        <button type="submit" name="cmd" value="save" ${jexlers.disabledIfReadonly}>Save as...</button>
        <button type="submit" name="cmd" value="delete" ${jexlers.disabledIfReadonly}>Delete...</button>
        <input type="text" name="jexler" value="${jexler.jexlerId}" ${jexlers.disabledIfReadonly}>
        <p></p>
        <textarea id="source" name="source">${jexler.source}</textarea>
      </form>
      <script>
        var editor = CodeMirror.fromTextArea(document.getElementById("source"), {
          lineNumbers: true,
          mode: "${jexler.mimeType}",
          tabMode: "indent",
          matchBrackets: true,
          indentUnit: 2,
          readOnly: ${!jexlers.allowScriptEdit}
        });
      </script>
    </c:otherwise>
  </c:choose>

  </body>

  </html>

</c:if>

</jsp:useBean>
