name := "user-service"

version := "0.1.17"

scalaVersion := "2.13.1"

/** Akka */
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"
)

/** Libs */
libraryDependencies += "com.pauldijou" %% "jwt-core" % "4.2.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "org.hashids" % "hashids" % "1.0.3"

/** Databases */
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "6.2.1",
  "mysql" % "mysql-connector-java" % "8.0.19",

  "org.tpolecat" %% "doobie-core" % "0.8.8",
  "org.tpolecat" %% "doobie-hikari" % "0.8.8",
)

/** Test */
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.1.0",
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.26",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"
)

enablePlugins(JavaAppPackaging)
enablePlugins(FlywayPlugin)

flywayUrl := "jdbc:mysql://localhost:3306"
flywayUser := "root"
flywayPassword := ""
flywayLocations += "migration"
flywaySchemas += "users"

parallelExecution in IntegrationTest := false
parallelExecution in Test := false

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

dockerRepository := Some("docker.pkg.github.com/two-app/user-service")
