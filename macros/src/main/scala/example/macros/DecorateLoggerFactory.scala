package example.macros

import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object DecorateLoggerFactory {

  private val fqcn = "example.DecorateLogger"

  def getLogger(name: String): DecorateLogger[FieldBuilder] = {
    val coreLogger = CoreLoggerFactory.getLogger(fqcn, name)
    new DecorateLogger(coreLogger, FieldBuilder)
  }

  def getLogger(clazz: Class[_]): DecorateLogger[FieldBuilder] = {
    val coreLogger = CoreLoggerFactory.getLogger(fqcn, clazz.getName)
    new DecorateLogger(coreLogger, FieldBuilder)
  }

  def getLogger: DecorateLogger[FieldBuilder] = {
    val coreLogger = CoreLoggerFactory.getLogger(fqcn, Caller.resolveClassName)
    new DecorateLogger(coreLogger, FieldBuilder)
  }
}
