package db

import config.Config

object DatabaseConfig {

  val driver: String = Config.getProperty("db.driver")
  val jdbc: String = Config.getProperty("db.jdbc")
  val schema: String = Config.getProperty("db.schema")
  val jdbcWithSchema: String = s"${jdbc}/${schema}"
  val username: String = Config.getProperty("db.username")
  val password: String = Config.getProperty("db.password")
  
}
