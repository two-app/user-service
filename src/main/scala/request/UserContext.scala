package request

import akka.http.scaladsl.model.HttpRequest
import pdi.jwt.{Jwt, JwtOptions}
import response.ErrorResponse
import response.ErrorResponse.AuthorizationError
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserContext(uid: Int, pid: Option[Int], cid: Option[Int])

object UserContext {

  implicit val f: RootJsonFormat[UserContext] = jsonFormat3(UserContext.apply)

  def from(accessToken: String): Either[ErrorResponse, UserContext] = {
    Jwt.decode(accessToken, JwtOptions(signature = false, expiration = false, notBefore = false))
      .map(claim => claim.content.parseJson.convertTo[UserContext])
      .toOption.toRight(AuthorizationError("Invalid token format."))
  }

  def from(request: HttpRequest): Either[ErrorResponse, UserContext] = {
    extractToken(request).flatMap(token => this.from(token))
  }

  private def extractToken(request: HttpRequest): Either[AuthorizationError, String] = {
    request.headers.find(h => h.name().equalsIgnoreCase("Authorization"))
      .map(header => header.value)
      .toRight(AuthorizationError("Authorization not provided."))
      .filterOrElse(t => !t.isBlank, AuthorizationError("Authorization not provided."))
      .map(header => header.split(" "))
      .filterOrElse(parts => parts.length == 2 && parts(0) == "Bearer", AuthorizationError("Invalid header format."))
      .map(parts => parts(1))
  }
}
