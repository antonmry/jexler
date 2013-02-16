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

<!-- SyntaxHighlighter -->
<script type="text/javascript" src="sh/scripts/shCore.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushBash.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushCpp.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushCSharp.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushCss.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushDelphi.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushDiff.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushGroovy.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushJava.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushJScript.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushPhp.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushPlain.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushPython.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushRuby.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushScala.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushSql.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushVb.js"></script>
<script type="text/javascript" src="sh/scripts/shBrushXml.js"></script>
<link type="text/css" rel="stylesheet" href="sh/styles/shCore.css"/>
<link type="text/css" rel="stylesheet" href="sh/styles/shThemeEclipse.css"/>
<script type="text/javascript">
  SyntaxHighlighter.config.clipboardSwf = 'sh/scripts/clipboard.swf';
  SyntaxHighlighter.all();
</script>

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

<c:if test="${param.cmd == 'info'}">
  <c:set var="jexler" value="${jexlers.jexlers[param.jexler]}"/>

  <h3>${jexler.id}</h3>

  ${jexler.fileText}

</c:if>

</jsp:useBean>

</body>

</html>