package example.macros

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

import java.util.Objects

trait DumpFieldBuilder extends FieldBuilder {
  import DumpFieldBuilder.impl

  def valdef(fieldName: String, instance: Seq[ValDefInspection]): Field = {
    `obj`(fieldName, instance.map {
      case ValDefInspection(name, value) =>
        keyValue(name, Objects.toString(value))
    })
  }

  def dumpPublicFields[A](instance: A): Seq[ValDefInspection] = macro impl.dumpPublicFields[A]
}

object DumpFieldBuilder extends DumpFieldBuilder {

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
        val fields = classVals(classType)

        c.Expr[Seq[ValDefInspection]](q"Seq(..$fields)")
      }
    }
}
