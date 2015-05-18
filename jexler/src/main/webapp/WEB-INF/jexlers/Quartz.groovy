[ "autostart" : true ]

services.add(new QuartzService(jexler, "every-15-secs").setQuartz("0/15 * * * * ?"))
services.start()

while (true) {
  event = events.take();
  if (event instanceof QuartzEvent) {
    log.trace("It is now: " + new Date() + " (" + event.service.id + ")")
  } else if (event instanceof StopEvent) {
    return
  }
}
