import io.getquill.{MysqlJdbcContext, SnakeCase}

package object db {

  lazy val ctx = new MysqlJdbcContext(SnakeCase, "ctx")

}
