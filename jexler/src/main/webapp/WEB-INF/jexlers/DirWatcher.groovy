[ "autostart" : true ]

services.add(new DirWatchService(jexler, "watch-jexler-dir"))
services.start()

while (true) {
  event = events.take();
  if (event instanceof DirWatchService.Event) {
    log.trace("Got file change: ${event.kind} '${event.file.name}'")
  } else if (event instanceof StopService.Event) {
    return
  }
}
