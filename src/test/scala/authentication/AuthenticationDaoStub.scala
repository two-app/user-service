package authentication

import pdi.jwt.Jwt
import cats.effect.Sync
import pdi.jwt.JwtClaim

class AuthenticationDaoStub[F[_]: Sync] extends AuthenticationDao[F] with AuthTestArbitraries {
  override def createTokens(
      uid: Int,
      pid: Option[Int],
      cid: Option[Int]
  ): F[Tokens] = {
    if (pid == None) {
      Sync[F].pure(
        Tokens(unconnectedJwt(uid, "testConnectCode"), "test-refresh-token")
      )
    } else {
      Sync[F].pure(
        Tokens(jwt(uid, pid.get, cid.get), "test-refresh-token")
      )
    }
  }

  override def storeCredentials(uid: Int, password: String): F[Tokens] =
    Sync[F].pure(
      Tokens(unconnectedJwt(uid, "testConnectCode"), "test-refresh-token")
    )
}