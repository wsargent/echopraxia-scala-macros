package example

object Main {
  private val logger = DecorateLoggerFactory.getLogger

  def main(args: Array[String]): Unit = {
    val foo = new Foo("fooName", 13)

    // This works fine
    logger.info {
      if (System.currentTimeMillis() - 1 == 0) {
        println("decorateIfs: if block")
      } else if (System.getProperty("derp") == null) {
        println("decorateIfs: derp is null")
      } else {
        println("decorateIfs: else block")
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
