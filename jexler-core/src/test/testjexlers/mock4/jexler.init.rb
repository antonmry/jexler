import Java::net.jexler.core.MockHandler
import Java::net.jexler.core.JexlerMessageFactory

$description = "Submits and handles"

h1 = MockHandler.new("h1", "Mock handler 1")
h2 = MockHandler.new("h2", "Mock handler 2")
h3 = MockHandler.new("h3", "Mock handler 3")

h2.canHandleAction = "true"
h2.handleAction = "false" # pass on
h3.submitMessageAtStartup = JexlerMessageFactory.create.set("info", "msg")
h3.canHandleAction = "true"
h3.handleAction = "true"

$handlers.add h1
$handlers.add h2
$handlers.add h3
