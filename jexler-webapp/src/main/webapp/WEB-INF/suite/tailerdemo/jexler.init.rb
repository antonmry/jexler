# test

import Java::java.util.HashMap
import Java::java.util.LinkedList
import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.FileTailerHandler

$description = "File tailer demo jexler"

config = HashMap.new
config.put("filetailerid", "selftailer")
$handlers.add ScriptHandler.new("filetailertest", "Handles 'selftailer' message by ruby script",
  $jexlerDir + "/handler_filetailer.rb", config)

fileToTail = "jexler.init.rb"
filters = LinkedList.new
filters.add("^import")
filters.add("!java\.util")
$handlers.add FileTailerHandler.new("selftailer", "Tails file " + fileToTail,
  $jexlerDir + "/" + fileToTail, filters)
