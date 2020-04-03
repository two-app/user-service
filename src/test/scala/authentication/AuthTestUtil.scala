package authentication

import pdi.jwt.Jwt
import pdi.jwt.JwtClaim
import akka.http.scaladsl.model.headers.RawHeader
import request.UserContext
import org.hashids.Hashids
import config.Config

trait AuthTestArbitraries {
  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  def unconnectedJwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim =
      JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )

  def authHeader(jwt: String): RawHeader =
    RawHeader("Authorization", s"Bearer $jwt")

  def extractContext(accessToken: String): UserContext =
    UserContext.from(accessToken).right.get

  def connectCodeFromId(id: Int): String =
    new Hashids(Config.getProperty("hashids.salt"), 6).encode(id)

  def connectCodeFromTokens(userTokens: Tokens): String =
    connectCodeFromId(
      UserContext.from(userTokens.accessToken).right.get.uid
    )

}

object AuthTestUtil {
  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  def unconnectedJwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim =
      JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )

  def authHeader(jwt: String): RawHeader =
    RawHeader("Authorization", s"Bearer $jwt")
}
