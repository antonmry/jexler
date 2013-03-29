<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.*" %>
<%@ page import="net.jexler.war.*" %>

<html>

<head>

<title>Jexler</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
<link rel="shortcut icon" href="favicon.ico"/>
<link rel="icon" href="favicon.ico"/>
<link rel="stylesheet" type="text/css" href="jexler.css"/>

<script src="cm/lib/codemirror.js"></script>
<link rel="stylesheet" href="cm/lib/codemirror.css">
<script src="cm/addon/edit/matchbrackets.js"></script>
<script src="cm/mode/ruby/ruby.js"></script>
<script src="cm/mode/python/python.js"></script>
<script src="cm/mode/groovy/groovy.js"></script>
<style type="text/css">.CodeMirror { border: 1px solid #eee; height: auto; } </style>

</head>

<body>

<jsp:useBean id="jexlers" class="net.jexler.war.JexlersView">

<a href='.'><img src='jexler.jpg' title='${jexlers.version}'></a>

<%= jexlers.handleCommands(request) %>

<table>

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
  <td title='${jexler.value.runStateInfo}'>${jexler.value.jexlerIdLink}</td>
  </tr>
</c:forEach>

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

</jsp:useBean>

</body>

</html>