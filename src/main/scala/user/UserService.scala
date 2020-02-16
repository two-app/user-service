package user

import authentication.{AuthenticationDao, Tokens}
import com.typesafe.scalalogging.Logger
import db.DatabaseError.{DuplicateEntry, Other}
import db.RecordMapper
import response.ErrorResponse
import response.ErrorResponse.{ClientError, InternalError, NotFoundError}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

object UserRecordMapper extends RecordMapper[UserRecord, Either[ModelValidationError, User]] {
  override def from(record: UserRecord): Either[ModelValidationError, User] = {
    User.from(record.uid, record.pid, record.cid, record.firstName, record.lastName)
  }

  override def to(model: Either[ModelValidationError, User]): UserRecord = null
}

trait UserService {
  def registerUser(ur: UserRegistration): Future[Either[ErrorResponse, Tokens]]

  def getUser(uid: Int): Future[Either[ErrorResponse, User]]
}

class UserServiceImpl(userDao: UserDao, authDao: AuthenticationDao) extends UserService {
  val logger: Logger = Logger(classOf[UserService])

  override def registerUser(ur: UserRegistration): Future[Either[ErrorResponse, Tokens]] = {
    logger.info(s"Registering user with email '${ur.email}'.'")
    userDao.storeUser(ur).flatMap {
      case Left(_: DuplicateEntry) => Future.successful(Left(ClientError("An account with this email exists.")))
      case Left(_: Other) => Future.successful(Left(InternalError()))
      case Right(uid: Int) => authDao.storeCredentials(uid, ur.password).map(t => Right(t))
    }
  }

  override def getUser(uid: Int): Future[Either[ErrorResponse, User]] = {
    logger.info(s"Retrieving user with UID $uid.")
    userDao.getUser(uid).map { maybeUser: Option[UserRecord] =>
      for {
        record <- maybeUser.toRight(NotFoundError(s"User with UID $uid does not exist."))
        user <- UserRecordMapper.from(record).left.map(e => ClientError(s"User record malformed. Reason: ${e.reason}"))
      } yield user
    }
  }
}
