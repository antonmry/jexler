import Java::net.jexler.StopService
import Java::net.jexler.FileTailerService

s = FileTailerService.new($jexler, "selftailer")
s.setFile($file.getAbsolutePath)
s.addFilterPattern("^import")
s.start
$services.add s

begin
  event = $events.take
  if event.is_a? FileTailerService::Event
    puts "Got line (ruby): " + event.getLine
  elsif event.is_a? StopService::Event
    return
  end
end while true
