import Java::net.jexler.FileTailerSensor
import Java::net.jexler.FileTailerEvent
import Java::net.jexler.StopEvent

fileTailerSensor = FileTailerSensor.new($jexler)
fileTailerSensor.setFile($file.getAbsolutePath)
fileTailerSensor.addFilterPattern("^import")
fileTailerSensor.start

begin
  event = $events.take
  if event.is_a? FileTailerEvent
    puts "Got line: " + event.getLine
  elsif event.is_a? StopEvent
    fileTailerSensor.stop
    return 0
  end
end while true
