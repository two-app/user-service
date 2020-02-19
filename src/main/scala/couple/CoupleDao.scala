package couple

import com.typesafe.scalalogging.Logger
import db.ctx._
import user.UserRecord

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

final case class CoupleRecord(cid: Int, uid: Int, pid: Int)

object CoupleRecord {
  def asInsertRecord(uid: Int, pid: Int): CoupleRecord = CoupleRecord(0, uid, pid)
}

trait CoupleDao {
  def storeCouple(uid: Int, pid: Int): Future[Int]

  def getCouple(cid: Int): Future[Option[CoupleRecord]]

  def connectUserToPartner(uid: Int, pid: Int, cid: Int): Future[Unit]
}

class QuillCoupleDao extends CoupleDao {
  val logger: Logger = Logger(classOf[QuillCoupleDao])

  override def storeCouple(uid: Int, pid: Int): Future[Int] = {
    logger.info(s"Storing new couple with UID $uid and PID $pid.")
    run(quote {
      querySchema[CoupleRecord]("couple").insert(lift(CoupleRecord.asInsertRecord(uid, pid))).returningGenerated(_.cid)
    })
  }

  override def getCouple(cid: Int): Future[Option[CoupleRecord]] = {
    logger.info(s"Retrieving couple by CID $cid.")
    run(quote {
      querySchema[CoupleRecord]("couple").filter(_.cid == lift(cid))
    }).map(r => r.headOption)
  }

  override def connectUserToPartner(uid: Int, pid: Int, cid: Int): Future[Unit] = {
    logger.info(s"Connecting UID $uid to PID $pid.")
    run(quote {
      querySchema[UserRecord]("user")
        .filter(_.uid == lift(uid))
        .update(_.pid -> lift(Option(pid)), _.cid -> lift(Option(cid)))
    }).map(_ => ())
  }
}
