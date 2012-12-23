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

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.charset.StandardCharsets.UTF_8
import com.treode.cps.{CpsKit, CpsSocketKit, cut, thunk}
import com.treode.cps.buffer.{Buffer, InputBuffer}
import com.treode.cps.io.{ServerSocket, Socket}
import com.treode.cps.scheduler.Scheduler
import com.treode.cps.CpsSocketKit

trait ExampleKit {

  // The Example provides the server and client, but it requires a runtime system to provide a
  // scheduler and sockets.  The test specifies a stub runtime system to facilitation testing, and
  // the scripts provide the live runtime system for actual operation.
  this: CpsKit with CpsSocketKit =>

  private [example] def readString (buf: InputBuffer) = {
    if (buf.ensure (4) < 0) throw new ClosedChannelException
    val len = buf.readInt
    if (buf.ensure (len) < 0) throw new ClosedChannelException
    val bytes = ByteBuffer.allocate (len)
    buf.readBytes (bytes)
    bytes.flip()
    UTF_8 .decode (bytes) .toString
  }

  private [example] def writeString (sock: Socket, str: String) = {
    val enc = UTF_8.encode (str)
    val buf = Buffer()
    buf.writeInt (enc.limit)
    buf.writeBytes (enc)
    val bytes = buf.readableByteBuffers
    sock.write (bytes)
    while (bytes .map (_.remaining) .sum > 0)
      sock.write (bytes)
  }

  // The CPS plugin has some trouble with try/catch.  To work around it when it happens, which is
  // not always, pull the try/catch block into its own method.
  def clientCatcher (client: Socket, input: InputBuffer) =
    try {
      val string = readString (input)
      writeString (client, string)
      input.discard (input.readAt)
    } catch {
      case _ :ClosedChannelException => cut()
    }

  def serveClient (client: Socket) {
    scheduler.spawn {
      val input = InputBuffer (client)
      while (client.isOpen) {
        clientCatcher (client, input)
      }}}

  def launchServer (addr: SocketAddress) {
    scheduler.spawn {
      val server = newServerSocket
      server.bind (addr)
      println ("Listening on " + server.localAddress.get)
      while (true) {
        serveClient (server.accept())
      }}}

  private def time (s: String, warmup: Int, trials: Int) (f: => Any @thunk) = {
    println ("Starting " + s + " warmup")
    var i = 0
    while (i < warmup) {
      f
      i += 1
    }
    println ("Starting " + s + " timing")
    var start = System.currentTimeMillis
    i = 0
    while (i < trials) {
      f
      i += 1
      if (i % 1000 == 0 || i == trials) {
        val n = if (i % 1000 == 0) 1000 else i % 1000
        val end = System.currentTimeMillis
        val ms = (end - start) .toDouble / n.toDouble
        val qps = n.toDouble / (end - start) .toDouble * 1000.0
        println ("%20s (%d trials): %10.3f ms, %10.0f qps" format (s, n, ms, qps))
        start = System.currentTimeMillis
      }}}

  def launchClient (addr: SocketAddress) {
    scheduler.spawn {
      val client = newSocket
      client.connect (addr)
      val input = InputBuffer (client)
      time ("echo", 1000, 100000) {
        writeString (client, "Hello")
        val string = readString (input)
        assert ("Hello" == string)
      }
      shutdown()
    }}}
