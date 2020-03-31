package partner

import authentication.{AuthenticationDao, Tokens}
import cats.data.EitherT
import cats.implicits._
import com.typesafe.scalalogging.Logger
import couple.CoupleDao
import response.ErrorResponse
import response.ErrorResponse.ClientError
import user.UserService

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import cats.Monad
import response.ErrorResponse.NotFoundError

trait PartnerService[F[_]] {
  def connectUsers(uid: Int, pid: Int): EitherT[F, ErrorResponse, Tokens]
}

class PartnerServiceImpl[F[_]: Monad](
    userService: UserService[F],
    coupleDao: CoupleDao[F],
    authDao: AuthenticationDao[F]
) extends PartnerService[F] {
  val logger: Logger = Logger(classOf[PartnerService[F]])

  override def connectUsers(
      uid: Int,
      pid: Int
  ): EitherT[F, ErrorResponse, Tokens] = {
    logger.info(s"Connecting users $uid and $pid.")
    for {
      user <- userService
        .getUser(uid)
        .ensure(ClientError("User already has a partner."))(_.pid.isEmpty)
      partner <- userService
        .getUser(pid)
        .leftMap {case NotFoundError(e) => NotFoundError("Partner does not exist.")}
        .ensure(ClientError("Partner already has a partner."))(_.pid.isEmpty)
      cid <- EitherT.right[ErrorResponse](coupleDao.storeCouple(uid, pid))
      _ <- EitherT.right[ErrorResponse](
        coupleDao.connectUserToPartner(uid, pid, cid)
      )
      _ <- EitherT.right[ErrorResponse](
        coupleDao.connectUserToPartner(pid, uid, cid)
      )
      newTokens <- EitherT.right[ErrorResponse](
        authDao.createTokens(uid, Option(pid), Option(cid))
      )
    } yield newTokens
  }
}
