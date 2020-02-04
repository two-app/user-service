package user

import db.RecordMapper
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

object UserRecordMapper extends RecordMapper[UserRecord, Either[InvalidUserError, User]] {
  override def from(record: UserRecord): Either[InvalidUserError, User] = {
    User.from(record.uid, record.firstName, record.lastName)
  }

  override def to(model: Either[InvalidUserError, User]): UserRecord = null
}

trait UserService {
  def getUser(uid: Int): Future[Either[ErrorResponse, User]]
}

class UserServiceImpl(userDao: UserDao) extends UserService {
  def getUser(uid: Int): Future[Either[ErrorResponse, User]] = {
    userDao.getUser(uid).map { maybeUser: Option[UserRecord] =>
      for {
        record <- maybeUser.toRight(NotFoundError(s"User with UID $uid does not exist."))
        user <- UserRecordMapper.from(record).left.map(e => ClientError(s"User record malformed. Reason: ${e.reason}"))
      } yield user
    }
  }
}
