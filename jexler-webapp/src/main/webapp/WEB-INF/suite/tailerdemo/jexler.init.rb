# test

import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.FileTailerHandler

$description = "File tailer demo jexler"

h = ScriptHandler.new("filetailertest", "Handles 'selftailer' message by ruby script")
h.setScriptFile($jexlerDir + "/handler_filetailer.rb")
h.set("filetailerid", "selftailer")
$handlers.add h

fileToTail = "jexler.init.rb"
h = FileTailerHandler.new("selftailer", "Tails file " + fileToTail)
h.setFile($jexlerDir + "/" + fileToTail)
h.addFilterPattern("^import")
h.addFilterPattern("!java\.util")
$handlers.add h
