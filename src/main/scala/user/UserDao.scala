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

object UserRecordMapper extends RecordMapper[UserRecord, Either[InvalidUserError, User]] {
  override def fromRecord(record: UserRecord): Either[InvalidUserError, User] = {
    User.from(record.uid, record.firstName, record.lastName)
  }

  override def toRecord(model: Either[InvalidUserError, User]): UserRecord = null
}

object UserDao {
  def getUser: Option[Either[InvalidUserError, User]] = run(quote {
    querySchema[UserRecord]("user").filter(u => u.email == "1998Gerry@gmail.com")
  }).headOption.map(r => UserRecordMapper.fromRecord(r))
}