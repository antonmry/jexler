import Java::net.jexler.handler.FileTailerHandler

if $method == "canHandle"
  if $message.get("sender").is_a? FileTailerHandler
    if $message.get("id") == $config.get("id")
      return true
    end
  end
elsif $method == "handle"
  puts "got line: " + $message.get("line")
  $log.info "### got line: " + $message.get("line")
end
