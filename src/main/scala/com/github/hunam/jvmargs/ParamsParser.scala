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
import scala.util.parsing.combinator.JavaTokenParsers

object ParamsParser extends JavaTokenParsers with ParamValueParsers {
  private def prelude = "[Global flags]".named("prelude")

  private val assigned = (":"?) <~ "=" ^^ { _.isDefined }

  private val value = """[\S&&[^{]]*""".r.named("value")

  private val categories = ("{" ~> (ident+) <~ "}").named("categories")

  val param =
    ident ~ ident ~ assigned ~ value ~ categories ^^ {
      case tpe ~ name ~ assigned ~ value ~ categories =>
        val valueParser: Parser[Any] = valueParsers(tpe)
        val parsedValue = parse(valueParser, value).get
        Param(tpe, name, assigned, parsedValue, categories)
    }

  val params = prelude ~> (param*)

  def parse(source: String): Map[String, Param[Any]] = parseAll(params, source).get.map(p => p.name -> p).toMap

}
