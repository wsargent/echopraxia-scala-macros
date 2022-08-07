package example.macros

import com.tersesystems.echopraxia.api.CoreLogger
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, FieldBuilder}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

class DumpLogger[FB <: FieldBuilder](core: CoreLogger, fieldBuilder: FB) extends AbstractLoggerSupport(core, fieldBuilder) {
  import DumpLogger.impl

  /**
   * Prints out the variable name
   *
   * {{{
   * core.log(Level.DEBUG, "{}", _.obj(nameOf(variable), variable))
   * }}}
   */
  def debug[A](expr: A): Unit = macro impl.debug[A]

}

object DumpLogger {

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def debug[A: c.WeakTypeTag](expr: c.Tree) = {
      @tailrec def extract(tree: c.Tree): String = tree match {
        case Ident(n) => n.decodedName.toString
        case Select(_, n) => n.decodedName.toString
        case Function(_, body) => extract(body)
        case Block(_, expr) => extract(expr)
        case Apply(func, _) => extract(func)
        case TypeApply(func, _) => extract(func)
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported expression: ${expr}")
      }

      val name = expr match {
        case Literal(Constant(_)) => c.abort(c.enclosingPosition, "Cannot provide name to static constant!")
        case _ => extract(expr)
      }

      // this gives us "main"
      //val logger = c.internal.enclosingOwner.asTerm
      val logger = q"dumpLogger"
      val level = q"com.tersesystems.echopraxia.api.Level.DEBUG"

      q"""$logger.core.log($level, "{}", fb => fb.keyValue($name, fb.dumpPublicFields($expr)))"""
    }
  }
}