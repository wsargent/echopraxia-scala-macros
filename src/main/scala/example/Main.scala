package example

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import example.macros.{DecorateLoggerFactory, DumpFieldBuilder}

object Main {
  private val decorate = DecorateLoggerFactory.getLogger
  private val dump = LoggerFactory.getLogger.withFieldBuilder(DumpFieldBuilder)

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
    dump.info("dumping fields {}", fb => fb.valdef("foo", fb.dumpPublicFields(foo)))
  }
}

class Foo(val name: String, val age: Int) {
  override def toString: String = s"Foo = ${name} ${age}"
}
