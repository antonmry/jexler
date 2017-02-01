static def hello() {
  // Jex.vars gives access to the binding in other classes
  Jex.vars.log.trace("Started by jexler '${Jex.vars.jexler.id}'")
  return "Hello World"
}
