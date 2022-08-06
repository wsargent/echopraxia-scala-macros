package example

import example.macros._
import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Level}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

import scala.language.experimental.macros

class DecorateLogger[FB <: FieldBuilder](core: CoreLogger, fieldBuilder: FB) extends DecorateMacros {

  //  def debug[A](statement: A): A = {
  //    decorateIfs(dif => {
  //      // Why doesn't this work?
  //      //      val function: Function[FB, FieldBuilderResult] = (fb: FB) => fb.list {
  //      //        ("code" -> dif.code)
  //      //        ("result" -> dif.result)
  //      //      }
  //
  //      val function: java.util.function.Function[FB, FieldBuilderResult] = (fb: FB) =>
  //        fb.list(
  //          fb.string("code" -> dif.code),
  //          fb.bool("result" -> dif.result)
  //        )
  //      core.log(Level.DEBUG, "{} => {}", function, fieldBuilder)
  //    })(statement)
  //  }

  def info[A](statement: A): A = macro decorateIfs

}
