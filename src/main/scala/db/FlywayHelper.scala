package db

import org.flywaydb.core.Flyway
import config.Config
import javax.sql.DataSource
import com.typesafe.scalalogging.Logger

object FlywayHelper {

  val logger: Logger = Logger("FlywayHelper")

  def migrate(dataSource: DataSource): Unit = {
    logger.info("Applying Flyway Migrations...")
    configureFlyway(dataSource).migrate()
    logger.info("Successfully applied migrations.")
  }

  private def configureFlyway(dataSource: DataSource): Flyway = {
    logger.info("Loading flyway connection...")
    Flyway
      .configure()
      .dataSource(dataSource)
      .locations("migration")
      .schemas(DatabaseConfig.schema)
      .defaultSchema(DatabaseConfig.schema)
      .load()
  }
}
