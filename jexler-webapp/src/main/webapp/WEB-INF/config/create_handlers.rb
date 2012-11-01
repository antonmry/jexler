# test

import Java::java.util.HashMap
import Java::java.util.LinkedList
import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.FileTailerHandler

config = HashMap.new
config.put("cronid", "nagnag")
$handlers.add ScriptHandler.new("crontest", "Handles 'nagnag' cron message by ruby script",
  $configDir + "/handler_cron.rb", config)

$handlers.add CronHandler.new("nagnag", "Sends every minute a cron message",
  "* * * * *")

config = HashMap.new
config.put("filetailerid", "selftailer")
$handlers.add ScriptHandler.new("filetailertest", "Handles 'selftailer' message by ruby script",
  $configDir + "/handler_filetailer.rb", config)

fileToTail = "create_handlers.rb"
filters = LinkedList.new
filters.add("^import")
filters.add("!java\.util")
$handlers.add FileTailerHandler.new("selftailer", "Tails file " + fileToTail,
  $configDir + "/" + fileToTail, filters)

