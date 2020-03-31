package db

import cats.effect.{Blocker, ContextShift, IO}
import config.Config
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

object TransactorUtil {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  def transactor(): Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    Config.getProperty("db.driver"),
    Config.getProperty("db.jdbc"),
    Config.getProperty("db.username"),
    Config.getProperty("db.password"),
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )
}
