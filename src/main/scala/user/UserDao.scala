package user

import java.util.Date

import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.typesafe.scalalogging.Logger
import db.DatabaseError
import db.ctx._

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

case class UserRecord
(
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

object UserRecord {
  def asInsertRecord(ur: UserRegistration): UserRecord = {
    new UserRecord(0, None, None, ur.email, ur.firstName, ur.lastName, ur.acceptedTerms, ur.ofAge, new Date())
  }
}

trait UserDao {
  def storeUser(ur: UserRegistration): Future[Either[DatabaseError, Int]]

  def getUser(uid: Int): Future[Option[UserRecord]]
}

class QuillUserDao extends UserDao {
  val logger: Logger = Logger(classOf[QuillUserDao])

  override def storeUser(ur: UserRegistration): Future[Either[DatabaseError, Int]] = {
    logger.info(s"Storing user.")
    run(quote {
      querySchema[UserRecord]("user").insert(lift(UserRecord.asInsertRecord(ur))).returningGenerated(_.uid)
    }).map(Right(_)).recover {
      case e: MySQLException => Left(DatabaseError.fromException(e))
      case e: Exception =>
        logger.error("Unknown Exception", e)
        Left(DatabaseError.Other())
    }
  }

  override def getUser(uid: Int): Future[Option[UserRecord]] = {
    logger.info(s"Retrieving user by UID $uid.")

    run(quote {
      querySchema[UserRecord]("user").filter(u => u.uid == lift(uid))
    }).map(r => r.headOption)
  }

}