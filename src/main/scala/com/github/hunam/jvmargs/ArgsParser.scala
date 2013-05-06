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
import scala.language.existentials
import scala.util.parsing.combinator.JavaTokenParsers

class ArgsParser(params: Map[String, Param[Any]]) extends JavaTokenParsers with ArgValueParsers {
  override def skipWhitespace = false

  def parse(cmdLine: String) =
    cmdLine.split("""\p{Space}+""") map (cmdLineArg => parseAll(arg, cmdLineArg))

  def arg = xxArg | dArg | xArg

  private object Named { def unapply(name: String) = params.get(name) }

  private val rhs = ("=" ~> """\S*""".r).named("rhs")

  private val xxArg = "-XX:" ~> guard((bool?) ~> ident <~ (rhs?) | failure("can't have both +/- and value"))
    .filter(name => params.contains(name)).withFailureMessage("unknown parameter") >> { name =>
      params(name).tpe match {
        case "bool" => boolXxArg
        case tpe => rhsXxArg(tpe)
      }
  }

  private val boolXxArg = bool ~ ident ^^ { case enabled ~ Named(param) => param assign enabled }

  private def rhsXxArg(tpe: String) = {
    val valueParser = valueParsers(tpe)
    ident ~ ("=" ~> valueParser) ^^ { case Named(param) ~ value => param assign value }
  }

  val dArg = "-D" ~> commit(repsep(ident, ".") ~ opt(("=") ~> commit(ccstr))).named("sysprop")
    .withFailureMessage("failed to validate system property")

  val xArg = "-X" ~> not("X:") ~> commit(xmx | xms).named("xarg")
    .withFailureMessage("failed to validate -X argument")

  val xmx = ("mx" ~> (":"?) ~> commit(size)).named("-Xmx")

  val xms = ("ms" ~> (":"?) ~> commit(size)).named("-Xmx")


}

object ArgsParser {
  def parse(params: Map[String, Param[Any]], cmdLine: String) =
    (new ArgsParser(params)).parse(cmdLine)
}