package example.macros

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, FieldBuilder}

import scala.language.experimental.macros

class DecorateLogger[FB <: FieldBuilder](core: CoreLogger, fieldBuilder: FB) extends AbstractLoggerSupport(core, fieldBuilder) {
  import DecorateLogger.impl
  import scala.language.experimental.macros

  def info[A](statement: A): A = macro impl.decorateIfs

  private def newLogger(coreLogger: CoreLogger): DecorateLogger[FB] =
    new DecorateLogger(coreLogger, fieldBuilder)

  def withFieldBuilder[NEWFB <: FieldBuilder](fb: NEWFB): DecorateLogger[NEWFB] =
    new DecorateLogger(core, fb)

  def withCondition(scalaCondition: Condition): DecorateLogger[FB] =
    newLogger(core.withCondition(scalaCondition.asJava))

  def withFields(f: FB => FieldBuilderResult): DecorateLogger[FB] = {
    import scala.compat.java8.FunctionConverters._
    newLogger(core.withFields(f.asJava, fieldBuilder))
  }

  def withThreadContext: DecorateLogger[FB] = {
    newLogger(core.withThreadContext(Utilities.threadContext()))
  }
}

object DecorateLogger {
  import scala.reflect.api.Trees
  import scala.reflect.macros.blackbox

  private class impl(val c: blackbox.Context) {

    import c.universe._

    def debug(dif: c.Expr[BranchInspection]) = {
      q"""debug("code = {} result = {}", fb => fb.list(fb.string("code", $dif.code), fb.bool("result", $dif.result)))"""
    }

    def decorateIfs(statement: c.Tree): c.Tree = {
      val output: c.Tree = q"""println"""
      println(statement)
      statement match {
        case q"if ($cond) $thenp else $elsep" =>
          val condSource = extractRange(cond) getOrElse ""
          val printThen =
            q"$output(example.macros.BranchInspection($condSource, true))"
          val elseThen =
            q"$output(example.macros.BranchInspection($condSource, false))"
          val decElseP = decorateIfs(elsep.asInstanceOf[c.Tree])

          val thenTree = q"""{ $printThen; $thenp }"""
          val elseTree = if (isEmpty(decElseP)) decElseP else q"""{ $elseThen; $decElseP }"""
          q"if ($cond) $thenTree else $elseTree"
        case other =>
          println(other)
          other
      }
    }

    private def isEmpty(tree: Trees#Tree): Boolean = {
      tree match {
        case Literal(Constant(())) =>
          true
        case other =>
          false
      }
    }

    def decorateMatch(output: c.Expr[BranchInspection => Unit])(matchStatement: c.Tree): c.Tree = {
      matchStatement match {
        case q"$expr match { case ..$cases }" =>
          val enhancedCases = cases.map {
            case CaseDef(pat, guard, body) =>
              val exprSource = extractRange(expr) getOrElse ""
              val patSource = extractRange(pat).map(p => s" match case $p") getOrElse ""
              val guardSource = extractRange(guard).map(" if " + _).getOrElse("")
              val src = exprSource + patSource + guardSource
              val debugIf = q"example.macros.BranchInspection($src, true)"
              val stmt = q"$output($debugIf); $body"
              CaseDef(pat, guard, stmt)
            case other =>
              throw new IllegalStateException("Unknown case " + other)
          }
          q"$expr match { case ..$enhancedCases }"
        case other =>
          other
      }
    }

    def decorateVals[A](output: c.Expr[ValDefInspection => Unit])(block: c.Expr[A]): c.Expr[A] = {
      val loggedStats = block.tree.children.flatMap {
        case valdef@ValDef(_, termName, _, _) =>
          List(
            valdef,
            q"$output(example.macros.ValDefInspection(${termName.encodedName.toString}, $termName))"
          )
        case stat =>
          List(stat)
      }
      val outputExpr: c.Expr[A] = c.Expr[A](c.untypecheck(q"..$loggedStats"))
      outputExpr
    }

    private def extractRange(t: Trees#Tree): Option[String] = {
      val pos = t.pos
      val source = pos.source.content
      if (pos.isRange) Option(new String(source.drop(pos.start).take(pos.end - pos.start)))
      else None
    }
  }

}


/**
 * Debugs a branch (if / match).
 *
 * @param code the condition of the branch
 * @param result the result of evaluating the condition
 */
case class BranchInspection(code: String, result: Boolean)

/**
 * Debugs a result.
 *
 * @param code the code that went into the result
 * @param value the result
 * @tparam A the type of the result.
 */
case class ExprInspection[A](code: String, value: A)
