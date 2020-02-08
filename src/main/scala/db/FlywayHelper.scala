package db

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object FlywayHelper {

  private var flyway: Option[Flyway] = None

  def getFlyway: Flyway = {
    if (flyway.isEmpty) {
      val dbUrl: String = ConfigFactory.load().getString("db.flywayUrl")
      val schema: String = ConfigFactory.load().getString("db.schema")
      flyway = Option(Flyway.configure()
        .locations("migration")
        .schemas(schema)
        .dataSource(dbUrl, "root", "")
        .load())
    }

    flyway.get
  }
}
