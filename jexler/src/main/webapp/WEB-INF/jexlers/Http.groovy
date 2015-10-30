[ "autostart" : true ]

JexlerDispatcher.dispatch(this)

void declare() {}

void start() {}

// Invoked when accessing the jexler webapp with ?cmd=http&jexler=<jexler>
// The single parameter 'p' is a JSP PageContext.
// (The handler is invoked independently of normal event handling in jexler,
// in separate threads, one for each parallel HTTP request.)
void handleHttp(def p) {
  log.trace("-- handleHttp")

  if (p.request.parameters.throw != null) {
    throw new RuntimeException("*** Failed intentionally - due to request parameter 'throw' ***")
  }

  def sysProps = new StringBuilder()
  System.properties.sort().each { key, value ->
    sysProps.append("<font color='blue'>$key</font><font color='red'>=</font>$value\n")
  }

  p.response.status = 200
  p.out.println("""\
<html>
  <head>
    <title>Jexler Http</title>
  </head>
  <body>
    <a href="."><img src="jexler.jpg"></a>
    <h1>Jexler Http</h1>
    <p>demo: <a href="?cmd=http&jexler=${jexler.id}&throw=true"><font color="red">throw exception</font></a></p>
    <h3>System Properties</h3>
    <pre>
${sysProps.toString()}
    </pre>
  </body>
</html>
""")
}

void handle(def event) {
  log.trace("got event $event.service.id")
}

void stop() {}