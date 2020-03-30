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
    userService
      .getUser(uid)
      .ensure(ClientError("User already has a partner."))(u => u.pid.isEmpty)
      .flatMap(_ => userService.getUser(pid))
      .ensure(ClientError("Partner already has a partner."))(p => p.pid.isEmpty)
      .flatMap(_ =>
        EitherT.right[ErrorResponse](coupleDao.storeCouple(uid, pid))
      )
      .map(cid => {
        coupleDao.connectUserToPartner(uid, pid, cid)
        coupleDao.connectUserToPartner(pid, uid, cid)
        cid
      })
      .flatMap(cid =>
        EitherT.right(authDao.createTokens(uid, Option(pid), Option(cid)))
      )
  }
}
