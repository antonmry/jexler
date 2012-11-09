<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.*" %>
<%@ page import="net.jexler.core.*" %>
<%@ page import="net.jexler.webapp.*" %>

<html>

<head>
<title>Jexler</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
<link rel="shortcut icon" href="favicon.ico"/>
<link rel="icon" href="favicon.ico"/>
<link rel="stylesheet" type="text/css" href="jexler.css"/>

<!-- syntaxhighlighter js -->
<script type="text/javascript" src="shl/scripts/shCore.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushBash.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushCpp.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushCSharp.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushCss.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushDelphi.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushDiff.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushGroovy.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushJava.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushJScript.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushPhp.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushPlain.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushPython.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushRuby.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushScala.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushSql.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushVb.js"></script>
<script type="text/javascript" src="shl/scripts/shBrushXml.js"></script>
<link type="text/css" rel="stylesheet" href="shl/styles/shCore.css"/>
<link type="text/css" rel="stylesheet" href="shl/styles/shThemeEclipse.css"/>
<script type="text/javascript">
  SyntaxHighlighter.config.clipboardSwf = 'shl/scripts/clipboard.swf';
  SyntaxHighlighter.all();
</script>

</head>

<body>

<a href='/'><img src='jexler.jpg'></a>

<jsp:useBean id="suite" class="net.jexler.webapp.JexlerSuiteControl">

<%= suite.handleCommands(request) %>

<table>

<tr>
<td>${suite.startStop}</td>
<td>${suite.restart}</td>
<td><strong>ID</strong></td>
<td><strong>Description</strong></td>
</tr>

<c:forEach items="${suite.jexlerControls}" var="jexler">
  <tr>
  <td>${jexler.value.startStop}</td>
  <td>${jexler.value.restart}</td>
  <td>${jexler.value.idLink}</td>
  <td>${jexler.value.description}</td>
  </tr>
</c:forEach>

</table>

<c:if test="${param.cmd == 'info'}">
  <c:set var="jexler" value="${suite.jexlerControls[param.jexler]}"/>

  <h3>${jexler.id}</h3>

  <p/>

  <table>

  <tr>
  <td><strong>Handlers</strong></td>
  <td><strong>Class</strong></td>
  <td><strong>Description</strong></td>
  </tr>

  <c:forEach items="${jexler.handlers}" var="handler">
    <tr>
    <td>${handler.key}</td>
    <td>${handler.value.className}</td>
    <td>${handler.value.description}</td>
    </tr>
  </c:forEach>

  </table>

  <p/>

  <table>

  <tr>
  <td><strong>Config</strong></td>
  </tr>

  <c:forEach items="${jexler.configFiles}" var="configFile">
    <tr>
    <td>${configFile.value.nameLink}</td>
    </tr>
  </c:forEach>

  </table>

  <c:if test="${not empty param.file}">
    <c:set var="configFile" value="${jexler.configFiles[param.file]}"/>

    <h3>${configFile.name}</h3>

    ${configFile.text}
  </c:if>

</c:if>

</jsp:useBean>

</body>

</html>