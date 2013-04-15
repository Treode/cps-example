/* Copyright (C) 2012-2013 Treode, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.treode.cps.example

import scala.util.Random
import com.treode.cps.{CpsKit, CpsSocketKit}
import com.treode.cps.scalatest.CpsFlatSpec
import com.treode.cps.buffer.InputBuffer
import com.treode.cps.stub.io.{ServerSocketStub, SocketAddressStub, SocketStub}
import com.treode.cps.stub.scheduler.TestScheduler

// Use CpsFlatSpec rather than Scalatest's FlatSpec, as it provides a few methods to wrap @thunk.
// There is also a CpsPropSpec to complement Scalatest's PropSpec.
class ExampleSpec extends CpsFlatSpec {

  // We mix-in the sequential test scheduler to use a single threaded scheduler that handles
  // continuations tasks in FIFO order, and we mix-in the stub sockets to listen and connect on
  // simulated sockets.  Alternatively, we could mix-in the random test scheduler, which uses a
  // single threaded scheduler that handles tasks in a psuedo-random order.  When combined with
  // `forAll` from CpsPropSpec to generate the psuedo-random seed, one has a way to run a test
  // with tasks repeatably scheduled in a different orders.  This can detect some race conditions
  // in a repeatable and debuggable way.
  trait TestKit extends CpsKit with CpsSocketKit {

    implicit val random = new Random (0)

    implicit val scheduler = TestScheduler.sequential()
    def shutdown() = scheduler.shutdown()

    def newSocketAddress = SocketAddressStub()
    def newServerSocket = ServerSocketStub()
    def newSocket = SocketStub()
  }

  class Kit extends ExampleKit with TestKit

  // Use "during" rather than "in".  It wraps @thunk.
  "The server" should "echo the request" during {

    // Use "withScheduler".  It wraps the remainder of the test with code to run the tasks;
    // otherwise nothing ever gets scheduled, and the test does not actually do anything.
    val kit = new Kit
    val scheduelr = withScheduler (kit.scheduler)

    val addr = kit.newSocketAddress
    kit.launchServer (addr)
    val client = kit.newSocket
    client.connect (addr)
    val input = InputBuffer (client)
    kit.writeString (client, "Hello")
    expectResult ("Hello") (kit.readString (input))
  }}
