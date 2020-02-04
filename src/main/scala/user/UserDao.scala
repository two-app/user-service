package user

import java.util.Date

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
  override def getUser(uid: Int): Future[Option[UserRecord]] = run(quote {
    querySchema[UserRecord]("user").filter(u => u.uid == lift(uid))
  }).map(r => r.headOption)
}