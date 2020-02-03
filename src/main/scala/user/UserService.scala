package user

import db.RecordMapper
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}

object UserRecordMapper extends RecordMapper[UserRecord, Either[InvalidUserError, User]] {
  override def from(record: UserRecord): Either[InvalidUserError, User] = {
    User.from(record.uid, record.firstName, record.lastName)
  }

  override def to(model: Either[InvalidUserError, User]): UserRecord = null
}

object UserService {
  def getUser(uid: Int): Either[ErrorResponse, User] = {
    val userRecord: Option[UserRecord] = UserDao.getUser(uid)
    if (userRecord.isEmpty) return Left(NotFoundError(s"User with UID $uid does not exist."))

    UserRecordMapper.from(userRecord.get) match {
      case Left(e) => Left(ClientError(s"User record malformed. Reason: ${e.reason}."))
      case Right(u) => Right(u)
    }
  }
}
