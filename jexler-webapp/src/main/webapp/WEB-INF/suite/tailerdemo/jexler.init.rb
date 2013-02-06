import Java::net.jexler.handler.FileTailerHandler
import Java::net.jexler.handler.ScriptHandler

$description = "File tailer demo jexler"

id = "filetailer" # handler id and id in sent messages
fileToTail = "jexler.init.rb" # tail this very file
h = FileTailerHandler.new(id, "Tails file " + fileToTail)
h.setFile($jexlerDir + "/" + fileToTail)
h.addFilterPattern("^import")
h.addFilterPattern("!java\.util")
$handlers.add h

h = ScriptHandler.new("filetailer-demo", "Handles filetailer message by script")
h.setScriptFile($jexlerDir + "/handler_filetailer.rb")
h.set("id", id) # message id to handle
$handlers.add h
