[ "autostart" : true, "autoimport" : false ]

import net.jexler.service.FileTailerService
import net.jexler.service.StopService

new FileTailerService(jexler, "selftailer").with {
  setFile jexler.file.absolutePath
  addFilterPattern "^import"
  services.add(it)
}
services.start()

while (true) {
  event = events.take();
  if (event instanceof FileTailerService.Event) {
    println ("Got line: " + event.line)
  } else if (event instanceof StopService.Event) {
    return
  }
}
