ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val echopraxiaPlusScalaVersion = "1.0.0"
val echopraxiaVersion = "2.1.1"

lazy val inspections = (project in file("inspections"))
  .settings(
    name := "inspection",
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
  )


lazy val root = (project in file("."))
  .settings(
    name := "untitled1",

    scalacOptions += "-Ymacro-debug-lite",

    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    libraryDependencies +="com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
  ).dependsOn(inspections).aggregate(inspections)
