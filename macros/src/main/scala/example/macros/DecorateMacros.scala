package example.macros

trait DecorateMacros {

  import scala.language.experimental.macros
  import DecorateMacros.impl

  /**
   * Decorates the given if statement with logging statements.
   *
   * For example, the following code:
   *
   * {{{
   * decorateIfs(dif: BranchInspection => logger.debug(s"\${dif.code} = \${dif.result}")) {
   *   if (System.currentTimeMillis() % 17 == 0) {
   *     println("branch 1")
   *   } else if (System.getProperty("derp") == null) {
   *     println("branch 2")
   *   } else {
   *     println("else branch")
   *   }
   * }
   * }}}
   *
   * would cause the logger to debug two lines:
   *
   * "System.currentTimeMillis() % 17 == 0 = false"
   * "System.getProperty("derp") == null = true"
   *
   * @param output      the logging statement to apply on each branch.
   * @param ifStatement the if statement to decorate with statements
   * @tparam A the result type
   * @return the result of the if statement.
   */
  def decorateIfs[A](output: BranchInspection => Unit)(ifStatement: A): A = macro impl.decorateIfs

  /**
   * Decorates a match statement with logging statements at each case.
   *
   * For example, given the following code:
   *
   * {{{
   * val string = java.time.Instant.now().toString
   * decorateMatch(dm: BranchInspection => logger.debug(s"\${dm.code} = \${dm.result}")) {
   *   string match {
   *     case s if s.startsWith("20") =>
   *       println("this example is still valid")
   *     case _ =>
   *       println("oh dear")
   *   }
   * }
   * }}}
   *
   * This will log the following at DEBUG level:
   *
   * "string match case s if s.startsWith("20") = true"
   *
   * @param output         the logging statement to apply to each case
   * @param matchStatement the match statement to decorate with statements
   * @tparam A the result type
   * @return
   */
  def decorateMatch[A](output: BranchInspection => Unit)(matchStatement: A): A =
  macro impl.decorateMatch

  /**
   * Decorates a given block with logging statements after each `val` or `var` (technically a `ValDef`).
   *
   * For example, given the following statement:
   *
   * {{{
   * decorateVals(dval: ValDefInspection => logger.debug(s"\${dval.name} = \${dval.value}")) {
   *   val a = 5
   *   val b = 15
   *   a + b
   * }
   * }}}
   *
   * There would be two statements logged at DEBUG level:
   *
   * "a = 5"
   * "b = 15"
   *
   * @param output the logging statement to put after each ValDef
   * @param block  the block to decorate with logging statements.
   * @tparam A the result type
   * @return the result, if any
   */
  def decorateVals[A](output: ValDefInspection => Unit)(block: A): A = macro impl.decorateVals[A]

}

object DecorateMacros extends DecorateMacros {
  import scala.reflect.api.Trees
  import scala.reflect.macros.blackbox

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def decorateIfs(output: c.Expr[BranchInspection => Unit])(ifStatement: c.Tree): c.Tree = {
      ifStatement match {
        case q"if ($cond) $thenp else $elsep" =>
          val condSource = extractRange(cond) getOrElse ""
          val printThen =
            q"$output(example.macros.BranchInspection($condSource, true))"
          val elseThen =
            q"$output(example.macros.BranchInspection($condSource, false))"
          val decElseP = decorateIfs(output)(elsep.asInstanceOf[c.Tree])

          val thenTree = q"""{ $printThen; $thenp }"""
          val elseTree = if (isEmpty(decElseP)) decElseP else q"""{ $elseThen; $decElseP }"""
          q"if ($cond) $thenTree else $elseTree"
        case other =>
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
              val exprSource  = extractRange(expr) getOrElse ""
              val patSource   = extractRange(pat).map(p => s" match case $p") getOrElse ""
              val guardSource = extractRange(guard).map(" if " + _).getOrElse("")
              val src         = exprSource + patSource + guardSource
              val debugIf = q"example.macros.BranchInspection($src, true)"
              val stmt    = q"$output($debugIf); $body"
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
        case valdef @ ValDef(_, termName, _, _) =>
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
      val pos    = t.pos
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
