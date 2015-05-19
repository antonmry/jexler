[ "autostart" : true ]


// "old-style" cron string with 5 items
services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
// "quartz-style" cron string with 6 (or 7) items, first item is for seconds
services.add(new CronService(jexler, "every-30-seconds").setCron("*/30 * * * * ?"))
services.add(new CronService(jexler, "once-immediately").setCron("now"))
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
