ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val echopraxiaPlusScalaVersion = "1.0.0"
val echopraxiaVersion = "2.1.1"

lazy val macros = (project in file("macros"))
  .settings(
    name := "macros",
    scalacOptions += "-language:experimental.macros",
    libraryDependencies ++= {
      // Compile / scalafmtConfig := file(".scalafmt-dotty.conf")
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, 0)) => Seq.empty
        case _ =>
          Seq(
            "org.scala-lang" % "scala-reflect" % scalaVersion.value
          )
      }
    },
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion
  )

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-macros",
    runMain := "example.Main",

    scalacOptions += "-Ymacro-debug-lite",
    scalacOptions += "-language:experimental.macros",
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    //libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    libraryDependencies +="com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
  ).dependsOn(macros).aggregate(macros)
