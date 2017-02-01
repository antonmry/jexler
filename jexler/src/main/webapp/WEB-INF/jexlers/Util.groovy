static def hello() {
  // Jex.vars gives access to the script binding in classes other than the started jexler script
  Jex.vars.log.trace("Started by jexler '${Jex.vars.jexler.id}'")
  return "Hello World"
}
