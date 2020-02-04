name := "user-service"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.26"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.26"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"

libraryDependencies += "com.pauldijou" %% "jwt-core" % "4.2.0"

/** Databases */
libraryDependencies += "org.flywaydb" % "flyway-core" % "6.2.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.19"
libraryDependencies += "io.getquill" % "quill-async-mysql_2.12" % "3.5.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"

enablePlugins(FlywayPlugin)

flywayUrl := "jdbc:mysql://localhost:3306"
flywayUser := "root"
flywayPassword := ""
flywayLocations += "migration"
flywaySchemas += "users"
