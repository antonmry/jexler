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

<a href='.'><img src='jexler.jpg'></a>

<jsp:useBean id="jexlers" class="net.jexler.war.JexlersView">

<%= jexlers.handleCommands(request) %>

<table>

<tr>
<td>${jexlers.startStop}</td>
<td>${jexlers.restart}</td>
<td><strong>Name</strong></td>
</tr>

<c:forEach items="${jexlers.jexlers}" var="jexler">
  <tr>
  <td>${jexler.value.startStop}</td>
  <td>${jexler.value.restart}</td>
  <td>${jexler.value.idLink}</td>
  </tr>
</c:forEach>

</table>

<%-- <c:if test="${param.cmd == 'info'}"> --%>
  <c:set var="jexler" value="${jexlers.jexlers[param.jexler]}"/>
  <p></p>
  <form method="get">
    <input type="submit" name="cmd" value="Save as...">
    <input type="submit" name="cmd" value="Delete...">
    <input type="text" name="jexler" value="${jexler.id}">
    <p></p>
  <textarea id="source" name="source">${jexler.source}</textarea>
  </form>
  <script>
    var editor = CodeMirror.fromTextArea(document.getElementById("source"), {
      lineNumbers: true,
      mode: "${jexler.mimeType}",
      tabMode: "indent",
      matchBrackets: true,
      indentUnit: 2
    });
  </script>

<%-- </c:if> --%>

</jsp:useBean>

</body>

</html>