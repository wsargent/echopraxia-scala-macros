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
    
    def error[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.ERROR"

      handle(tpeA, level, expr)
    }

    def warn[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.WARN"

      handle(tpeA, level, expr)
    }

    def info[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.INFO"

      handle(tpeA, level, expr)
    }

    def debug[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.DEBUG"

      handle(tpeA, level, expr)
    }

    def trace[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.TRACE"

      handle(tpeA, level, expr)
    }

    private def handle(tpeA: Type, level: Select, expr: c.Tree) = {
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
      
      val logger = c.prefix
      val fieldBuilderType = tq"$logger.fieldBuilder.type"      
      val function = q"""new java.util.function.Function[$fieldBuilderType, com.tersesystems.echopraxia.api.FieldBuilderResult]() {
        def apply(fb: $fieldBuilderType) = fb.keyValue($name, fb.ToValue[$tpeA]($expr))
      }"""
      q"""$logger.core.log($level, "{}", $function, $logger.fieldBuilder)"""
    }
  }
}


// [Seq[com.tersesystems.echopraxia.api.Field]]
/*
"message": "overloaded method log with alternatives:\n  [FB](x$1: com.tersesystems.echopraxia.api.Level, 
x$2: java.util.function.Supplier[java.util.List[com.tersesystems.echopraxia.api.Field]], x$3: com.tersesystems.echopraxia.api.Condition, x$4: String, x$5: java.util.function.Function[FB,com.tersesystems.echopraxia.api.FieldBuilderResult], x$6: FB): Unit <and>\n  [FB](x$1: com.tersesystems.echopraxia.api.Level, x$2: com.tersesystems.echopraxia.api.Condition, x$3: String, x$4: java.util.function.Function[FB,com.tersesystems.echopraxia.api.FieldBuilderResult], x$5: FB): Unit <and>\n  (x$1: com.tersesystems.echopraxia.api.Level,x$2: java.util.function.Supplier[java.util.List[com.tersesystems.echopraxia.api.Field]],x$3: com.tersesystems.echopraxia.api.Condition,x$4: String)Unit <and>\n  (x$1: com.tersesystems.echopraxia.api.Level,x$2: com.tersesystems.echopraxia.api.Condition,x$3: String)Unit <and>\n  [FB](x$1: com.tersesystems.echopraxia.api.Level, x$2: java.util.function.Supplier[java.util.List[com.tersesystems.echopraxia.api.Field]], x$3: String, x$4: java.util.function.Function[FB,com.tersesystems.echopraxia.api.FieldBuilderResult], x$5: FB): Unit <and>\n  [FB](x$1: com.tersesystems.echopraxia.api.Level, x$2: String, x$3: java.util.function.Function[FB,com.tersesystems.echopraxia.api.FieldBuilderResult], x$4: FB): Unit <and>\n  (x$1: com.tersesystems.echopraxia.api.Level,x$2: java.util.function.Supplier[java.util.List[com.tersesystems.echopraxia.api.Field]],x$3: String)Unit <and>\n  (x$1: com.tersesystems.echopraxia.api.Level,x$2: String)Unit\n cannot be applied to (com.tersesystems.echopraxia.api.Level, String, example.MyFieldBuilder.type => Seq[com.tersesystems.echopraxia.api.Field])",
*/
