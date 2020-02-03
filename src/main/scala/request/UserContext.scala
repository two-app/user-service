package request

import akka.http.scaladsl.model.HttpRequest
import response.ErrorResponse

case class UserContext(uid: Int)

object UserContext {
  def apply(request: HttpRequest): Either[ErrorResponse, UserContext] = {
    Right(new UserContext(1))
  }
}
