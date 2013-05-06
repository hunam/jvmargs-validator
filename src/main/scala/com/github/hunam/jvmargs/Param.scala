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

import scala.util.parsing.input.Positional

case class Param[+T](tpe: String, name: String, assigned: Boolean,
                      value: T, categories: List[String]) extends Positional {

  def assign[S >: T](newValue: S) = copy(assigned = true, value = newValue)

  override def toString = {
    val enabled = (tpe, value) match {
      case ("bool", true) => "+"
      case ("bool", false) => "-"
      case _ => ""
    }
    val rhs = (tpe, value) match {
      case ("bool", _: Boolean) => ""
      case ("ccstrlist", ccstrlist: List[_/*String*/]) =>
        "=" + ccstrlist.mkString(",")
      case _ => s"=$value"
    }
    s"-XX:${enabled}${name}${rhs}"
  }
}
