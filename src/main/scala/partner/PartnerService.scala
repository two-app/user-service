package partner

import authentication.{AuthenticationDao, Tokens}
import couple.CoupleDao
import response.ErrorResponse
import response.ErrorResponse.ClientError
import user.UserService

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

trait PartnerService {
  def connectUsers(uid: Int, pid: Int): Future[Either[ErrorResponse, Tokens]]
}

class PartnerServiceImpl(userService: UserService, coupleDao: CoupleDao, authDao: AuthenticationDao) extends PartnerService {
  override def connectUsers(uid: Int, pid: Int): Future[Either[ErrorResponse, Tokens]] = {
    userService.getUser(uid)
      .map(errorOrUser => errorOrUser.filterOrElse(u => u.pid.isEmpty, ClientError("User already has a partner.")))
      .flatMap(_ => userService.getUser(pid))
      .map(errorOrPartner => errorOrPartner.filterOrElse(p => p.pid.isEmpty, ClientError("Partner already has a partner.")))
      .flatMap(_ => coupleDao.storeCouple(uid, pid))
      .flatMap(cid => {
        for {
          _ <- coupleDao.connectUserToPartner(uid, pid, cid)
          _ <- coupleDao.connectUserToPartner(pid, uid, cid)
        } yield cid
      })
      .flatMap(cid => authDao.createTokens(uid, Option(pid), Option(cid)))
      .map(Right(_))
  }
}
