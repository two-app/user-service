package db

import org.flywaydb.core.Flyway
import config.Config
import javax.sql.DataSource
import com.typesafe.scalalogging.Logger

object FlywayHelper {
  val logger: Logger = Logger("FlywayHelper")

  def migrate(): Unit = {
    logger.info(
      s"Applying Flyway migrations with default schema ${DatabaseConfig.schema}."
    )
    
    Flyway
      .configure()
      .dataSource(
        DatabaseConfig.jdbc,
        DatabaseConfig.username,
        DatabaseConfig.password
      )
      .schemas(DatabaseConfig.schema)
      .defaultSchema(DatabaseConfig.schema)
      .locations("migration")
      .load()
      .migrate()
  }
}
