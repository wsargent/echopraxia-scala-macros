package example

import com.tersesystems.echopraxia.api.CoreLoggerFactory
import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder
import com.tersesystems.echopraxia.plusscala.generic.AutoDerivation
import example.macros.{DecorateLoggerFactory, DumpFieldBuilder, DumpLogger}

object Main {
  private val decorate = DecorateLoggerFactory.getLogger
  private val loggerWithDumpFieldBuilder = LoggerFactory.getLogger.withFieldBuilder(DumpFieldBuilder)

  val coreLogger = CoreLoggerFactory.getLogger(this.getClass.getName, this.getClass.getName)
  private val dumpLogger = new DumpLogger(coreLogger, DumpFieldBuilder)

  def main(args: Array[String]): Unit = {

    decorate.info {
      if (System.currentTimeMillis() - 1 == 0) {
        println("decorateIfs: if block")
      } else if (System.getProperty("derp") == null) {
        println("decorateIfs: derp is null")
      } else {
        println("decorateIfs: else block")
      }
    }

    val foo = new Foo("fooName", 13)
    loggerWithDumpFieldBuilder.info("dumping fields {}", fb => fb.valdef("foo", fb.dumpPublicFields(foo)))

    dumpLogger.debug(foo)
  }
}

class Foo(val name: String, val age: Int) {
  override def toString: String = s"Foo = ${name} ${age}"
}

trait MyFieldBuilder extends FieldBuilder {

}

object MyFieldBuilder extends MyFieldBuilder