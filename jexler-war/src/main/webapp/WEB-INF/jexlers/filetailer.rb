import Java::net.jexler.StopSensor
import Java::net.jexler.FileTailerSensor

s = FileTailerSensor.new($jexler, "selftailer")
s.setFile($file.getAbsolutePath)
s.addFilterPattern("^import")
s.start
$sensors.add s

begin
  event = $events.take
  if event.is_a? FileTailerSensor::Event
    puts "Got line (ruby): " + event.getLine
  elsif event.is_a? StopSensor::Event
    return
  end
end while true
