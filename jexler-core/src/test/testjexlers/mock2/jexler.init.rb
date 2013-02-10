import Java::net.jexler.core.MockHandler

$description = "Throws at start and stop"

h1 = MockHandler.new("h1", "Mock handler 1")
h2 = MockHandler.new("h2", "Mock handler 2")
h3 = MockHandler.new("h3", "Mock handler 3")

h3.startAction = "throw"
h2.stopAction = "throw"

$handlers.add h1
$handlers.add h2
$handlers.add h3
