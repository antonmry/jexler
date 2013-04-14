// autostart
import net.jexler.Jexler;
import net.jexler.service.StopService
import net.jexler.service.FileTailerService

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
