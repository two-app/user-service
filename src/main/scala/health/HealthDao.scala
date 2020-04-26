package health

import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.effect.Bracket
import cats.data.EitherT
import db.DatabaseError

trait HealthDao[F[_]] {
  def performSimpleStatement(): EitherT[F, DatabaseError, Int]
}

class DoobieHealthDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends HealthDao[F] {

  override def performSimpleStatement(): EitherT[F, DatabaseError, Int] = ???

}
