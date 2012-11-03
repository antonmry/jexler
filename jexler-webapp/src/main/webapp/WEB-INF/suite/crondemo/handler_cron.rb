# test

import Java::net.jexler.handler.CronHandler

$log.info "script handler_cron " + $method
if $method == "canHandle"
  if $message.get("sender").is_a? CronHandler
    if $message.get("id") == $config.get("cronid")
      return true
    end
  end
elsif $method == "handle"
  puts "It is now " + Time.new.inspect
end
