package health

import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.effect.Bracket
import cats.data.EitherT
import java.sql.SQLException

trait HealthDao[F[_]] {
  def performSimpleStatement(): EitherT[F, SQLException, Int]
}

class DoobieHealthDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends HealthDao[F] {

  override def performSimpleStatement(): EitherT[F, SQLException, Int] = EitherT(
    sql"""SELECT 1""".query[Int].unique.attemptSql.transact(xa)
  )

}
