import cats.effect.{Async, Blocker, ContextShift, IO}
import config.Config
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

package object db {
  def transactor[F[_]: Async: ContextShift](): Aux[F, Unit] =
    Transactor.fromDriverManager[F](
      Config.getProperty("db.driver"),
      Config.getProperty("db.jdbc"),
      Config.getProperty("db.username"),
      Config.getProperty("db.password"),
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )
}
