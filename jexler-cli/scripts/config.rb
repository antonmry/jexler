# test

import Java::java.util.HashMap
import Java::net.jexler.handler.CommandLineHandler
import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

#$handlers.add JexlerSystemHandler.new("jexler", "Handles command line input")
$handlers.add CommandLineHandler.new("jexler", "Handles command line input")

$handlers.add CronHandler.new("jexler", "Sends every minute a cron message",
  "* * * * *", "test-cron-id")

config = HashMap.new
config.put("cronid", "test-cron-id")
$handlers.add ScriptHandler.new("jexler", "Handles cron message by ruby script",
  "ruby", "scripts/handler.rb", config)
