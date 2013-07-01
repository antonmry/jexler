[ "autostart" : true, "autoimport" : false ]

import net.jexler.service.FileTailService
import net.jexler.service.StopService

new FileTailService(jexler, "selftailer").with {
  setFile jexler.file
  addFilterPattern "^import"
  services.add(it)
}
services.start()

while (true) {
  event = events.take();
  if (event instanceof FileTailService.Event) {
    log.trace("Got line: " + event.line)
  } else if (event instanceof StopService.Event) {
    return
  }
}
