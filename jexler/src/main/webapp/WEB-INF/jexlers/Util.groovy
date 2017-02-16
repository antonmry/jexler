class Util {

  static def hello() {
    return 'Hello World'
  }
  
  // jexlerBinding gives access to jexler script binding
  // variables in other classes, even from a static context
  static def log = jexlerBinding.log
  static {
    log.trace("Class loaded for jexler '${jexlerBinding.jexler.id}'")
  }
  static logMethodCall() {
    log.trace("Method called for jexler '${jexlerBinding.jexler.id}'")
  }

}
