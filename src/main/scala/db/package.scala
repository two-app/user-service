import com.typesafe.config.ConfigFactory
import io.getquill._
import org.flywaydb.core.Flyway

package object db {

  lazy val ctx = new MysqlAsyncContext(SnakeCase, "ctx")

  val jdbcUrl: String = "jdbc:" + ConfigFactory.load().getString("ctx.url")

  def getFlyway: Flyway = Flyway.configure()
    .locations("migration")
    .dataSource(jdbcUrl, "root", "")
    .load()

}
