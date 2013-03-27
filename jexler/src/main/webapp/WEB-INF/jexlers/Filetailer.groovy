// autostart
import net.jexler.Jexler;
import net.jexler.StopService
import net.jexler.FileTailerService

s = new FileTailerService(jexler, "selftailer")
s.setFile(jexler.file.absolutePath)
s.addFilterPattern("^import")
s.start()
services.add(s)

while (true) {
  event = events.take();
  if (event instanceof FileTailerService.Event) {
    println ("Got line (groovy): " + event.line)
  } else if (event instanceof StopService.Event) {
    return
  }
}
