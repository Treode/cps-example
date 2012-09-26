# An example using the Treode CPS library.

The
[ExampleKit](https://github.com/Treode/cps-example/blob/master/src/main/scala/com/treode/cps/example/ExampleKit.scala)
shows how to assemble a basic echo server and client.  The
[ExampleSpec](https://github.com/Treode/cps-example/blob/master/src/test/scala/com/treode/cps/example/ExampleSpec.scala)
shows how to use the test utilities to test them.  It's important that the build file link the CPS
stub and scalatest packages into the test configuration, and the
[build file](https://github.com/Treode/cps-example/blob/master/project/CpsExampleBuild.scala)
show how to do that.
The scripts
[echo-server](https://github.com/Treode/cps-example/blob/master/scripts/echo-server) and
[echo-client](https://github.com/Treode/cps-example/blob/master/scripts/echo-client)
show how to bring up the server and run a client.
