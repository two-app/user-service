package user

import authentication.{AuthenticationDao, Tokens}
import cats.data.EitherT
import cats.implicits._
import com.typesafe.scalalogging.Logger
import db.RecordMapper
import response.ErrorResponse
import response.ErrorResponse.{ClientError, InternalError, NotFoundError}
import cats.Functor
import cats.Monad
import cats.data.OptionT
import cats.Applicative

object UserRecordMapper extends RecordMapper[UserRecord, Either[ModelValidationError, User]] {
  override def from(record: UserRecord): Either[ModelValidationError, User] = {
    User.from(record.uid, record.pid, record.cid, record.firstName, record.lastName)
  }

  override def to(model: Either[ModelValidationError, User]): UserRecord = null
}

trait UserService[F[_]] {
  def registerUser(ur: UserRegistration): EitherT[F, ErrorResponse, Tokens]

  def getUser(uid: Int): EitherT[F, ErrorResponse, User]

  def getUser(email: String): EitherT[F, ErrorResponse, User]
}

class UserServiceImpl[F[_]: Monad](userDao: UserDao[F], authDao: AuthenticationDao[F]) extends UserService[F] {
  val logger: Logger = Logger(classOf[UserService[F]])

  override def registerUser(ur: UserRegistration): EitherT[F, ErrorResponse, Tokens] = {
    logger.info(s"Registering user with email '${ur.email}'.'")
    
    for {
      uid <- userDao.storeUser(ur).leftMap(_ => ClientError("An account with this email exists."))
      tokens <- EitherT.right(authDao.storeCredentials(uid, ur.password))
    } yield tokens
  }

  override def getUser(uid: Int): EitherT[F, ErrorResponse, User] = {
    logger.info(s"Retrieving user by UID $uid.")
    this.handleMissingUser(
      userDao.getUser(uid)
    )
  }

  override def getUser(email: String): EitherT[F, ErrorResponse, User] = {
    logger.info(s"Retrieving user by email $email.")
    if (EmailValidator.isValid(email)) {
      handleMissingUser(userDao.getUser(email))
    } else {
      EitherT.leftT(ClientError("Badly formatted email."))
    }
  }

  def handleMissingUser(maybeUser: OptionT[F, UserRecord]): EitherT[F, ErrorResponse, User] = {
    for {
      record <- maybeUser.toRight(NotFoundError(s"User does not exist."))
      user <- UserRecordMapper.from(record)
        .leftMap[ErrorResponse](e => ClientError(s"User record malformed. Reason: ${e.reason}"))
        .toEitherT[F]
    } yield user
  }
}
