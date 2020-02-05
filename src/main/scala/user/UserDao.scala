package user

import java.util.Date

import com.typesafe.scalalogging.Logger
import db.ctx._

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

case class UserRecord(
                       uid: Int,
                       pid: Option[Int],
                       cid: Option[Int],
                       email: String,
                       firstName: String,
                       lastName: String,
                       acceptedTerms: Boolean,
                       ofAge: Boolean,
                       createdAt: Date
                     )

trait UserDao {
  def getUser(uid: Int): Future[Option[UserRecord]]
}

class QuillUserDao extends UserDao {
  val logger: Logger = Logger(classOf[QuillUserDao])

  override def getUser(uid: Int): Future[Option[UserRecord]] = {
    logger.info(s"Retrieving user by UID $uid.")

    run(quote {
      querySchema[UserRecord]("user").filter(u => u.uid == lift(uid))
    }).map(r => r.headOption)
  }
}