package example.macros

trait DumpMacros {
  import DumpMacros.impl

  import scala.language.experimental.macros

  /**
   * Dumps public fields from an object.
   *
   * {{{
   * class ExampleClass(val someInt: Int) {
   *   protected val protectedInt = 22
   * }
   * val exObj        = new ExampleClass(42)
   * val publicFields: Seq[ValDefInspection] = dumpPublicFields(exObj)
   *
   * logger.debug(publicFields.toString)
   * }}}
   *
   * Should result in:
   *
   * "Seq(DebugVal(someInt,42))"
   *
   * @param instance the object instance
   * @tparam A the type of the object
   * @return the `DebugVal` representing the public fields of the object.
   */
  def dumpPublicFields[A](instance: A): Seq[ValDefInspection] = macro impl.dumpPublicFields[A]
}

object DumpMacros extends DumpMacros {
  import scala.reflect.macros.blackbox

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def dumpPublicFields[A: WeakTypeTag](instance: c.Expr[A]): c.Expr[Seq[ValDefInspection]] = {
      def classVals(tpe: c.universe.Type) = {
        tpe.decls.collect {
          case method: MethodSymbol if method.isAccessor && method.isPublic =>
            val nameStr = method.name.decodedName.toString
            q"example.macros.ValDefInspection(${nameStr}, $instance.$method)"
        }
      }

      val classType = weakTypeTag[A].tpe
      val fields    = classVals(classType)

      c.Expr[Seq[ValDefInspection]](q"Seq(..$fields)")
    }
  }

}

/**
 * Debugs a valdef.
 *
 * @param name name of the val or var
 * @param value the value of the val or var
 */
case class ValDefInspection(name: String, value: Any)
