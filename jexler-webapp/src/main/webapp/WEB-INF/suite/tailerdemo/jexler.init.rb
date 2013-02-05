# test

import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.FileTailerHandler

$description = "File tailer demo jexler"

scriptHandler = ScriptHandler.new("filetailertest", "Handles 'selftailer' message by ruby script")
scriptHandler.setScriptFileName($jexlerDir + "/handler_filetailer.rb")
scriptHandler.set("filetailerid", "selftailer")
$handlers.add scriptHandler

fileToTail = "jexler.init.rb"
fileTailerHandler = FileTailerHandler.new("selftailer", "Tails file " + fileToTail)
fileTailerHandler.setFileName($jexlerDir + "/" + fileToTail)
fileTailerHandler.addFilterPatternString("^import")
fileTailerHandler.addFilterPatternString("!java\.util")
$handlers.add fileTailerHandler
