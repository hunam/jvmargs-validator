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

import scala.util.parsing.combinator.JavaTokenParsers
import scala.language.postfixOps

trait ValueParsers extends JavaTokenParsers {
  protected def bool: Parser[Boolean]

  protected def uintx: Parser[Long]

  protected def intx = ("""-?\d+""".r ^^ { _.toInt }).named("intx")
    .withFailureMessage("32-bit signed integer expected")

  protected def uint64_t = ("""\d+""".r ^^ { BigInt(_) }).named("uint64_t")
    .withFilter(_ < (BigInt(1) << 64))
    .withFailureMessage("64-bit unsigned integer expected")

  protected def double = (decimalNumber ^^ { BigDecimal(_) }).named("double")
    /* due to SI-6699: .withFilter(_.isValidDouble)*/.map(_.toDouble)
    .withFailureMessage("double-precision decimal expected")

  protected def ccstr = {
    (guard("\"") ~> commit(stringLiteral.withFailureMessage("quoted string expected"))) |
    """\S*""".r.withFailureMessage("string expected")
  }.named("ccstr")

  protected def ccstrlist = repsep("""[\S&&[^,"]]*""".r | stringLiteral, ",").named("ccstrlist")
    .withFailureMessage("comma-separated string list expected")

  protected lazy val valueParsers = Map(
    "bool" -> bool,
    "intx" -> intx,
    "uintx" -> uintx,
    "uint64_t" -> uint64_t,
    "double" -> double,
    "ccstr" -> ccstr,
    "ccstrlist" -> ccstrlist
  )
}

trait ParamValueParsers extends ValueParsers {
  protected override def bool = ("(true)|(false)".r ^^ { _ == "true" }).named("bool")
    .withFailureMessage("either 'true' or 'false'")

  protected override def uintx = ("""\d+""".r ^^ { BigInt(_) % (1l << 32) toLong }).named("uintx")
    .withFilter(_ < (1l << 32))
    .withFailureMessage("32-bit unsigned integer expected")
}

trait ArgValueParsers extends ValueParsers {
  protected override def bool = ("[-+]".r ^^ { _ == "+" }).named("bool")
    .withFailureMessage("either '-' or '+' expected")

  protected override def uintx = ("""\d+""".r ^^ { _.toLong  }).named("uintx")
    .withFilter(_ < (1l << 32))
    .withFailureMessage("32-bit unsigned integer expected")

  private def sizeOf(unit: String): Long = {
    unit.toLowerCase match {
      case "" => 1
      case "k" => 1024
      case "m" => 1048576
      case "g" => 1024 * 1048576
    }
  }

  protected def size: Parser[Long] = uintx ~ "[gGmMkK]?".r ^^ {
    case size ~ unit => size * sizeOf(unit)
  }
}