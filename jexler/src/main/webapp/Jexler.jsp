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
  <script src="jexler.js"></script>
  <script src="cm/lib/codemirror.js"></script>
  <script src="cm/addon/edit/matchbrackets.js"></script>
  <script src="cm/mode/groovy/groovy.js"></script>

  </head>

  <body onLoad="onPageLoad()">

  <form action="request.contextPath" method="post">
  
  <table class="frame">
  <tr id="header">
  <td class="frame">
  <a href="."><img src="jexler-mini.jpg" title="${container.jexlerTooltip}"></a>
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
        <td><button type="submit" name="cmd" value="save" ${container.disabledIfReadonly}
                    onclick="return isPostSave(${container.confirmSave}, '${jexler.jexlerId}')">Save as...</button></td>
        <td><button type="submit" name="cmd" value="delete" ${container.disabledIfReadonly}
                    onclick="return isPostDelete(${container.confirmDelete}, '${jexler.jexlerId}')">Delete...</button></td>
        <td><input id="newjexlername" type="text" name="jexler" onkeyup="updateSaveIndicator('${jexler.jexlerId}')"
                   value="${jexler.jexlerId}" ${container.disabledIfReadonly}></td>
        <td><img id="savestatus" src="space.gif"></td>
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

<tr class="status">
<td class="status">${container.startStopZap}</td>
<td class="status">${container.restart}</td>
<td class="status">${container.log}</td>
<td class="status status-name"><strong>Scripts</strong></td>
<td class="status">${container.web}</td>
</tr>

<c:forEach items="${container.jexlers}" var="loopJexler">
  <tr class="status">
  <td class="status">${loopJexler.value.startStopZap}</td>
  <td class="status">${loopJexler.value.restart}</td>
  <td class="status">${loopJexler.value.log}</td>
  <td class="status status-name" title="${loopJexler.value.stateInfo}">${loopJexler.value.jexlerIdLink}</td>
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
            updateSaveIndicator('${jexler.jexlerId}');
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
