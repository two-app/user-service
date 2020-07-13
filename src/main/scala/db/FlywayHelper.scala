package db

import com.typesafe.scalalogging.Logger
import org.flywaydb.core.Flyway

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
