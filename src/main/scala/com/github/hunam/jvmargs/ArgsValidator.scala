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

import scala.sys.process.Process
import scala.language.existentials

/**
 * Allows validation of JVM arguments. Collaborates with the specified JVM
 * executable to validate the specified command line parameters.
 *
 * @param javaPath The full path to the JVM executable.
 *                 Not providing a full path (e.g.: 'java', the default) will rely
 *                 on the system path instead.
 * @param cmdLine  The would-be command line arguments to be validated. This
 *                 would include anything in the command line, except for the
 *                 'java' executable specified above.
 */
class ArgsValidator(var javaPath: String = "java", var cmdLine: String = "") {

  /**
   * Extracts a parameter symbol table based on provided arguments and
   * the specified 'java' executable.
   *
   * @return A map of supported parameters, indexed by name.
   */
  def params(): Map[String, Param[Any]] = {
    // extract from 'cmdLine' only the arguments that are known
    // to provide additional parameters
    val unlocks = UnlocksParser.parse(cmdLine)

    // dumps a complete list of parameters supported by the JVM,
    // as affected by 'unlocks'
    val flagsFinal = Process(s"$javaPath $unlocks -XX:+PrintFlagsFinal -version")
      .lines.mkString("\n")

    // parses the dumped parameters into a Map[String, Param[Any]] --
    // essentially, a symbol table
    ParamsParser.parse(flagsFinal)
  }

  /**
   * Validates the class-provided command line parameters against the
   * specified 'java' executable.
   *
   * @return Sequence of parse results.
   *         An attempt is made to parse every argument.
   */
  def validate(): Seq[ArgsParser#ParseResult[Any]] =
    validate(params())

  private[jvmargs] def validate(params: Map[String, Param[Any]]): Seq[ArgsParser#ParseResult[Any]] =
    ArgsParser.parse(params, cmdLine)

  /**
   * Launches the specified JVM with command-line parameters, parsing
   * final parameter values.
   *
   * @return A map of parameters, indexed by name (only assigned parameters).
   */
  def assignedParams(): Map[String, Param[Any]] = {
    // dumps a complete list of parameters supported by the JVM,
    // as affected by 'unlocks'
    val flagsFinal = Process(s"$javaPath $cmdLine -XX:+PrintFlagsFinal -version")
      .lines.mkString("\n")

    // parses the dumped parameters into a Map[String, Param[Any]],
    // then returns only parameters that have been assigned non-default values.
    ParamsParser.parse(flagsFinal).filter(_._2.assigned)
  }
}
