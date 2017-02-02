class Util {

  static def hello() {
    return "Hello World"
  }
  
  // Jex.vars gives access to the script binding
  // in classes other than the started jexler script,
  // even from a static context
  static def log = Jex.vars.log
  static {
    log.trace("Class loaded for jexler '${Jex.vars.jexler.id}'")
  }
  static logMethodCall() {
    log.trace("Method called for jexler '${Jex.vars.jexler.id}'")
  }


}
