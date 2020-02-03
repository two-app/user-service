package user

import java.util.Date

import db.RecordMapper
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

object UserDao {
  def getUser: Option[UserRecord] = run(quote {
    querySchema[UserRecord]("user").filter(u => u.email == "1998Gerry@gmail.com")
  }).headOption
}