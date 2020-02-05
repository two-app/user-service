package user

import com.typesafe.scalalogging.Logger
import db.DatabaseError.{DuplicateEntry, Other}
import db.{DatabaseError, RecordMapper}
import response.ErrorResponse
import response.ErrorResponse.{ClientError, InternalError, NotFoundError}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

object UserRecordMapper extends RecordMapper[UserRecord, Either[InvalidUserError, User]] {
  override def from(record: UserRecord): Either[InvalidUserError, User] = {
    User.from(record.uid, record.firstName, record.lastName)
  }

  override def to(model: Either[InvalidUserError, User]): UserRecord = null
}

trait UserService {
  def registerUser(ur: UserRegistration): Future[Either[ErrorResponse, Int]]

  def getUser(uid: Int): Future[Either[ErrorResponse, User]]
}

class UserServiceImpl(userDao: UserDao) extends UserService {
  val logger: Logger = Logger(classOf[UserService])

  override def registerUser(ur: UserRegistration): Future[Either[ErrorResponse, Int]] = {
    logger.info(s"Registering user with email '${ur.email}'.'")
    userDao.storeUser(ur).map { errorOrUid: Either[DatabaseError, Int] =>
      errorOrUid.left.map {
        case _: DuplicateEntry => ClientError("An account with this email exists.")
        case _: Other => InternalError()
      }
    }
  }

  override def getUser(uid: Int): Future[Either[ErrorResponse, User]] = {
    logger.info(s"Retrieving user with UID ${uid}.")
    userDao.getUser(uid).map { maybeUser: Option[UserRecord] =>
      for {
        record <- maybeUser.toRight(NotFoundError(s"User with UID $uid does not exist."))
        user <- UserRecordMapper.from(record).left.map(e => ClientError(s"User record malformed. Reason: ${e.reason}"))
      } yield user
    }
  }
}
