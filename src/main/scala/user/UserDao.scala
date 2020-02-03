package user

import java.util.Date

import db.ctx._

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
  def getUser(uid: Int): Option[UserRecord]
}

class QuillUserDao extends UserDao {
  override def getUser(uid: Int): Option[UserRecord] = run(quote {
    querySchema[UserRecord]("user").filter(u => u.uid == lift(uid))
  }).headOption
}