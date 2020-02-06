name := "user-service"

version := "0.1"

scalaVersion := "2.12.10"

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

/** Databases */
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "6.2.1",
  "mysql" % "mysql-connector-java" % "8.0.19",
  "io.getquill" % "quill-async-mysql_2.12" % "3.5.0"
)

/** Test */
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.1.0",
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.26",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"
)

enablePlugins(FlywayPlugin)

flywayUrl := "jdbc:mysql://localhost:3306"
flywayUser := "root"
flywayPassword := ""
flywayLocations += "migration"
flywaySchemas += "users"

parallelExecution in IntegrationTest := false