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

import sbt._
import Keys._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin.assemblySettings

object CpsExampleBuild extends Build {

  lazy val exampleSettings =
    assemblySettings ++
  Seq (
    organization := "com.treode",
    name := "cps-example",
    version := "0.1",
    scalaVersion := "2.9.2",

    test in assembly := {},

    addCompilerPlugin ("org.scala-lang.plugins" % "continuations" % "2.9.2"),
    scalacOptions ++= Seq ("-P:continuations:enable", "-deprecation"),

    resolvers += Resolver.url (
      "treode-oss-releases",
      new URL ("http://treode.artifactoryonline.com/treode/oss-releases")) (Resolver.ivyStylePatterns),

    // We make the test configuration (of this project) depend on the scalatest
    // configuration (of the CPS dependency).  The CPS project provides testing
    // support in two additional configurations (stub and scalatest), so that
    // the testing support does not leak into your production code.  The stub
    // configuration contains testing stubs useful generally, and the scalatest
    // configuration includes those and adds additional support of interest only
    // to scalatest users.
    libraryDependencies ++= Seq (
      "com.treode" %% "cps" % "0.2.0" % "compile;test->scalatest"))

  lazy val root = Project ("root", file ("."))
    .settings (exampleSettings: _*)

}
