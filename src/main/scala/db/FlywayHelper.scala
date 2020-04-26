package db

import org.flywaydb.core.Flyway
import config.Config
import javax.sql.DataSource

object FlywayHelper {
  def migrate(dataSource: DataSource): Unit = {
    configureFlyway(dataSource).migrate()
  }

  private def configureFlyway(dataSource: DataSource): Flyway =
    Flyway
      .configure()
      .dataSource(dataSource)
      .locations("migration")
      .schemas(DatabaseConfig.schema)
      .defaultSchema(DatabaseConfig.schema)
      .load()
}
