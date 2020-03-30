package authentication

import pdi.jwt.Jwt
import pdi.jwt.JwtClaim

object AuthTestUtil {
  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  def unconnectedJwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim =
      JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )
}
