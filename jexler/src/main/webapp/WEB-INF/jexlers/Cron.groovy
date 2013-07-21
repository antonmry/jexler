[ "autostart" : true ]

services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
services.add(new CronService(jexler, "every-two-minutes").setCron("*/2 * * * *"))
services.add(new OnceService(jexler, "once"))
services.start()

while (true) {
  event = events.take();
  if (event instanceof CronEvent) {
    log.trace("It is now: " + new Date() + " (" + event.service.id + ")")
	log.trace("Util.hello(): " + Util.hello())
  } else if (event instanceof OnceEvent) {
    log.trace("log once")
  } else if (event instanceof StopEvent) {
    return
  }
}
