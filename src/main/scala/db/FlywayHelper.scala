package db

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object FlywayHelper {

  private var flyway: Option[Flyway] = None

  def getFlyway: Flyway = {
    if (flyway.isEmpty) {
      val jdbcUrl: String = "jdbc:" + ConfigFactory.load().getString("ctx.url")
      val password: String = sys.env.getOrElse("DB_PASSWORD", "")
      println("Connecting with JDBC: " + jdbcUrl)
      println(s"And password: '${password}'")
      flyway = Option(Flyway.configure()
        .locations("migration")
        .dataSource(jdbcUrl, "root", password)
        .load())
    }

    flyway.get
  }
}
