package db

import config.Config
import doobie.util.transactor.Transactor
import doobie.util._
import cats.effect._
import org.flywaydb.core.Flyway

trait DatabaseTestMixin {

  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    DatabaseConfig.driver,
    DatabaseConfig.jdbc,
    DatabaseConfig.username,
    DatabaseConfig.password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  val flyway: Flyway = Flyway
    .configure()
    .dataSource(
      DatabaseConfig.jdbcWithoutSchema,
      DatabaseConfig.username,
      DatabaseConfig.password
    )
    .locations("migration")
    .schemas(DatabaseConfig.schema)
    .load()

  def cleanMigrate(): Unit = {
    flyway.clean()
    flyway.migrate()
  }

}
