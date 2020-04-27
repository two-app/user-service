package partner

import request.UserContext
import cats.data.OptionT
import user.User
import cats.effect.Bracket
import doobie.util.transactor.Transactor
import db.DateTimeModule._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import com.typesafe.scalalogging.Logger

trait PartnerDao[F[_]] {
  def getPartnerId(uid: Int): OptionT[F, Int]
}

class DoobiePartnerDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends PartnerDao[F] {

  val logger: Logger = Logger[DoobiePartnerDao[F]]

  override def getPartnerId(uid: Int): OptionT[F, Int] = {
    logger.info(s"Retrieving partner of UID ${uid}")
    OptionT(
      PartnerSql.getPartnerId(uid).transact(xa)
    )
  }

}

object PartnerSql {

  def getPartnerId(uid: Int): ConnectionIO[Option[Int]] =
    sql"""
         | SELECT pid
         | FROM user
         | WHERE uid = $uid
         |""".stripMargin
      .query[Option[Int]]
      .unique

}
