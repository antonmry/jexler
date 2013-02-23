# autostart
import Java::net.jexler.StopService
import Java::net.jexler.CronService
import Java::net.jexler.ShellTool

$services.add CronService.new($jexler, "every-minute").setCron("* * * * *").start
$services.add CronService.new($jexler, "every-two-minutes").setCron("*/2 * * * *").start

begin
  event = $events.take
  if event.is_a? CronService::Event
    puts "It is now (ruby) " + Time.new.inspect + " (" + event.getServiceId + ")"

    shellTool = ShellTool.new
    result = shellTool.run "echo 'hello world'"
    puts "rc = " + result.rc.to_s
    puts "stdout = " + result.stdout
    puts "stderr = " + result.stderr

    #$log.debug "hello"
    raise "something goes wrong"

  elsif event.is_a? StopService::Event
    return
  end
end while true
