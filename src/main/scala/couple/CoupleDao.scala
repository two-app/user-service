package couple

import com.typesafe.scalalogging.Logger
import user.UserRecord
import cats.data.OptionT
import cats.effect.Bracket
import db.DateTimeModule._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import java.time.Instant
import cats.Monad

final case class CoupleRecord(cid: Int, uid: Int, pid: Int, created_at: Instant)

object CoupleRecord {
  def from(uid: Int, pid: Int): CoupleRecord =
    CoupleRecord(0, uid, pid, Instant.now())
}

trait CoupleDao[F[_]] {
  def storeCouple(uid: Int, pid: Int): F[Int]

  def getCouple(cid: Int): OptionT[F, CoupleRecord]

  def connectUserToPartner(uid: Int, pid: Int, cid: Int): F[Unit]
}

class DoobieCoupleDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends CoupleDao[F] {
  override def storeCouple(uid: Int, pid: Int): F[Int] =
    CoupleSql
      .insert(CoupleRecord.from(uid, pid))
      .transact(xa)

  override def getCouple(cid: Int): OptionT[F, CoupleRecord] = OptionT(
    CoupleSql.select(cid).transact(xa)
  )

  override def connectUserToPartner(uid: Int, pid: Int, cid: Int): F[Unit] = {
    connectionTransaction(uid, pid).transact(xa)
  }

  private def connectionTransaction(uid: Int, pid: Int): ConnectionIO[Unit] =
    for {
      updateUser <- CoupleSql.updateUserPartnerId(uid, pid)
      updatePartner <- CoupleSql.updateUserPartnerId(pid, uid)
    } yield ()
}

object CoupleSql {
  def insert(coupleRecord: CoupleRecord): ConnectionIO[Int] =
    sql"""
         | INSERT INTO couple (uid, pid, connected_at)
         | VALUES (${coupleRecord.uid}, ${coupleRecord.pid}, ${coupleRecord.created_at})
         |""".stripMargin.update.withUniqueGeneratedKeys[Int]("cid")

  def select(cid: Int): ConnectionIO[Option[CoupleRecord]] =
    sql"""
         | SELECT cid, uid, pid, connected_at
         | FROM couple
         | WHERE cid = $cid
         |""".stripMargin.query[CoupleRecord].option

  def updateUserPartnerId(uid: Int, pid: Int): ConnectionIO[Int] =
    sql"""
         | UPDATE user
         | SET pid = $pid
         | WHERE uid = $uid
         |""".stripMargin.update.run
}
