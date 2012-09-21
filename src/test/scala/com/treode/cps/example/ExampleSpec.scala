/* Copyright (C) 2012 Treode, Inc.
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

import com.treode.cps.{CpsFlatSpec, CpsSpecKit, CpsStubSocketKit}
import com.treode.cps.buffer.InputBuffer
import com.treode.cps.io.SocketAddressStub

// Use CpsFlatSpec rather than FlatSpec.  It provides a few methods to wrap @thunk.
class ExampleSpec extends CpsFlatSpec {

  class Kit extends CpsSpecKit.Sequential with CpsStubSocketKit with ExampleKit

  // Use "during" rather than "in".  It wraps @thunk.
  "The server" should "echo the request" during {
    // Use "withCpsKit".  It wraps the remainder of the test with code to run the tasks;
    // otherwise nothing ever gets scheduled, and the test does not actually do anything.
    val kit = withCpsKit (new Kit)
    val addr = SocketAddressStub (new Random (0), kit.scheduler)
    kit.launchServer (addr)
    val client = kit.newSocket
    client.connect (addr)
    val input = InputBuffer (client)
    kit.writeString (client, "Hello")
    expectResult ("Hello") (kit.readString (input))
  }}
