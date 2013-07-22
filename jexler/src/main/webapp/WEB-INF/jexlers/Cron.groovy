[ "autostart" : true ]

services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
services.add(new CronService(jexler, "every-two-minutes").setCron("*/2 * * * *"))
services.add(new CronService(jexler, "run once immediately").setCron("now"))
services.start()

while (true) {
  event = events.take();
  if (event instanceof CronEvent) {
    log.trace("It is now: " + new Date() + " (" + event.service.id + ")")
	log.trace("Util.hello(): " + Util.hello())
  } else if (event instanceof StopEvent) {
    return
  }
}
