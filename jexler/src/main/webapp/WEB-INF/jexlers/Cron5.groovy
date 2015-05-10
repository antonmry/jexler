// jexler

static def getConfig() return { [ "autostart" : true ] }

def start() {
  services.addCron("every-minute", "* * * * *")
  services.addCron("every-two-minutes", "*/2 * * * *")
  services.addCron("once-immediately", "now")
}

def handle(def event) {
  if (event instanceof CronEvent) {
    log.trace("It is now: " + new Date() + " (" + event.service.id + ")")
	log.trace("Util.hello(): " + Util.hello())
  }
}

def stop() {
}
