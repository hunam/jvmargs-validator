/*
 * Copyright (c) 2013 Nadav Wiener
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.hunam.jvmargs

import scala.language.postfixOps
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ArgsValidatorSpec extends FlatSpec with ShouldMatchers {

  val unlocks = "-XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions"

  val params = ArgsValidator.params("java", unlocks)

  "Validator" should "be able to validate all parameters with default values" in {
    val failures = params map {
      case (name, param) => (name, param.toString)
    } map {
      case (name, cmdLine) =>
        // using private 'validate(params, ...)' to prevent
        // launching JVM hundreds of times
        cmdLine -> ArgsValidator.validateStateless(params, "java", s"$unlocks $cmdLine")
    } flatMap {
      case (cmdLine, validations) if validations.length != 3 =>
        Some(s"$cmdLine has multiple validations: ${validations drop 2}")
      case (cmdLine, validations) if validations.drop(2).head.isEmpty =>
        Some(s"$cmdLine has failed validation: ${validations drop 2 head}")
      case (cmdLine, _) =>
        // println(s"$cmdLine successful") // <-- uncheck to see successful validations
        None
    }
    failures foreach println
    println(s"validated ${params.size} parameters with default values")
    assert(failures.isEmpty)
  }

  def valid(arg: String) = it should s"validate $arg" in {
    val validations = ArgsValidator.validateStateless(params, "java", arg)
    val failures = validations.filterNot(_.successful)
    println(failures)
    assert(failures.isEmpty)
  }

  def invalid(arg: String) = it should s"fail to validate $arg" in {
    val validations = ArgsValidator.validateStateless(params, "java", arg)
    val failures = validations.filterNot(_.successful)
    failures foreach println
    assert(failures.nonEmpty)
  }

  valid("-XX:MaxHeapSize=1000")
  invalid("-XX:MaxHeapSize")
  invalid("-XX:MaxHeapSize=test")
  invalid("-XX:MaxHeapSize=1.1")
  invalid("-XX:MaxHeapSize=\"test\"")
  invalid("-XX:+MaxHeapSize=3")
  invalid("-XX:+MaxHeapSize")
  invalid("-XX:-MaxHeapSize")
  valid("-XX:-UseCompressedOops")
  valid("-XX:+UseCompressedOops")
  invalid("-XX:UseCompressedOops")
  invalid("-XX:UseCompressedOops=")
  invalid("-XX:UseCompressedOops=3")
  invalid("-XX:UseCompressedOops=3.3")
  invalid("-XX:UseCompressedOops=true")
  invalid("-XX:UseCompressedOops=test")
  invalid("-XX:UseCompressedOops=\"test\"")
  invalid("-XX:Foobar")
  invalid("this is just a bunch of words")
  valid("-Xmx1g")
  valid("-Xmx1m -Xms1k -XX:+UseCompressedOops")
  valid("-Dfoo.bar=\"hello\"")
  valid("-Dfoo.bar=3")
  valid("-Dfoo.bar")
  valid("-Dfoo")
  valid("-D") // <-- turns out this is legal
}
