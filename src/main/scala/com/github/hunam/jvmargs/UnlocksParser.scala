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
import scala.util.parsing.combinator.RegexParsers

object UnlocksParser extends RegexParsers {
  val experimental = "-XX:+UnlockExperimentalVMOptions" ^^ { Some(_) }
  val diagnostic = "-XX:+UnlockDiagnosticVMOptions" ^^ { Some(_) }
  val skip = """\S+""".r ^^^ { None: Option[String] }
  val unlocks = ((experimental | diagnostic | skip)*) ^^ { _.flatten }
  def parse(cmdLine: String) = parseAll(unlocks, cmdLine).get.mkString(" ")
}
