import Java::net.jexler.handler.CronHandler

if $method == "canHandle"
  if $message.get("sender").is_a? CronHandler
    if $message.get("id") == $config.get("id")
      return true
    end
  end
elsif $method == "handle"
  puts "It is now " + Time.new.inspect
end
