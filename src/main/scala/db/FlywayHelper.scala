package db

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object FlywayHelper {

  private var flyway: Option[Flyway] = None

  def getFlyway: Flyway = {
    val jdbcUrl: String = "jdbc:" + ConfigFactory.load().getString("ctx.url")
    if (flyway.isEmpty) {
      flyway = Option(Flyway.configure()
        .locations("migration")
        .dataSource(jdbcUrl, "root", "")
        .load())
    }

    flyway.get
  }
}
