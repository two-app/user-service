import io.getquill._

package object db {

  lazy val ctx = new MysqlAsyncContext(SnakeCase, "ctx")

}
