package db

import config.Config

object DatabaseConfig {

  val driver: String = Config.getProperty("db.driver")
  val jdbc: String = Config.getProperty("db.jdbc")
  val jdbcWithoutSchema: String = Config.getProperty("db.jdbcWithoutSchema")
  val schema: String = Config.getProperty("db.schema")
  val username: String = Config.getProperty("db.username")
  val password: String = Config.getProperty("db.password")
  
}
