package example.macros

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

import java.util.Objects

trait MacroFieldBuilder extends FieldBuilder with DumpMacros {

  def valdef(fieldName: String, instance: Seq[ValDefInspection]): Field = {
    `obj`(fieldName, instance.map {
      case ValDefInspection(name, value) =>
        keyValue(name, Objects.toString(value))
    })
  }
}

object MacroFieldBuilder extends MacroFieldBuilder

