import Java::net.jexler.core.MockHandler
import Java::net.jexler.core.JexlerMessageFactory

$description = "Submits and handles"

h1 = MockHandler.new("h1", "Mock handler 1")
h2 = MockHandler.new("h2", "Mock handler 2")
h3 = MockHandler.new("h3", "Mock handler 3")

h1.canHandleAction = "true"
h1.handleAction = "false" # not done, pass on
h2.canHandleAction = "true"
h2.handleAction = "true" # done, do not pass on
h3.submitMessageAtStartup = JexlerMessageFactory.create.set("info", "msg")
h3.canHandleAction = "true"
h3.handleAction = "false" # not done, pass on

$handlers.add h1
$handlers.add h2
$handlers.add h3
