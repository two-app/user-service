import io.getquill._
import org.flywaydb.core.Flyway

package object db {

  lazy val ctx = new MysqlAsyncContext(SnakeCase, "ctx")

  def getFlyway: Flyway = Flyway.configure()
    .locations("migration")
    .dataSource("jdbc:mysql://localhost:3306/users?user=root", "root", "")
    .load()

}
