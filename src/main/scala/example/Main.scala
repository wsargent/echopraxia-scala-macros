package example

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import example.macros._

import java.util.Objects

object Main extends DecorateMacros {
  private val logger = LoggerFactory.getLogger.withFieldBuilder(NewFieldBuilder)

  def main(args: Array[String]): Unit = {
    val foo = new Foo("fooName", 13)
    logger.info("{}", fb => fb.valdef("fooPublicFields", fb.dumpPublicFields(foo)))

    // This works fine
    decorateIfs(dif => logger.debug(s"code = ${dif.code} result = ${dif.result}")) {
      if (System.currentTimeMillis() - 1 == 0) {
        assert("decorateIfs: if block" != null)
      } else if (System.getProperty("derp") == null) {
        assert("decorateIfs: derp is null" != null)
      } else {
        assert("decorateIfs: else block" != null)
      }
    }

    // XXX why doesn't the below work?
    //    scalac: Error
    //    : Could not find proxy
    //    for dif: example.macros.BranchInspection in List
    //    (value dif, method $anonfun$main$8, method main,
    //    object Main
    //    , package example
    //    , package <root>) (currentOwner= method $anonfun$main$9 )
    //      java.lang.IllegalArgumentException: Could not find proxy for dif: example.macros.BranchInspection in List(value dif, method $anonfun$main$8, method main, object Main, package example, package
    //      <root>) (currentOwner= method $anonfun$main$9 )
    //        at scala.tools.nsc.transform.LambdaLift$LambdaLifter.searchIn$1(LambdaLift.scala:319)
    /*
    decorateIfs(dif => logger.debug("code = {} result = {}", fb => fb.list(
      fb.string("code", dif.code), fb.bool("result", dif.result)))
    ) {
      if (System.currentTimeMillis() - 1 == 0) {
        assert("decorateIfs: if block" != null)
      } else if (System.getProperty("derp") == null) {
        assert("decorateIfs: derp is null" != null)
      } else {
        assert("decorateIfs: else block" != null)
      }
    }
    */
  }
}

class Foo(val name: String, val age: Int) {
  override def toString: String = s"Foo = ${name} ${age}"
}

trait NewFieldBuilder extends FieldBuilder with DumpMacros {

  def valdef(fieldName: String, instance: Seq[ValDefInspection]): Field = {
    `obj`(fieldName, instance.map {
      case ValDefInspection(name, value) =>
        keyValue(name, Objects.toString(value))
    })
  }
}

object NewFieldBuilder extends NewFieldBuilder

