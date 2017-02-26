// Utility class, available to all jexlers,
// compiled together with all other utility classes.

class Util {

  static def hello() {
    return 'Hello World'
  }
  
  static def log = JexlerContainer.getLogger()

  static logMethodCall(def script) {
    log.trace("Method called for jexler '$script.jexler.id'")
    script.log.trace("Method called...")
  }

}
