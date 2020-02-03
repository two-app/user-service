package response

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes

trait ErrorResponse {
  def status: StatusCode
  def reason: String
}

object ErrorResponse {
  case class AuthorizationError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.Unauthorized
  }

  case class InternalError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.InternalServerError
  }

  case class InvalidDataError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.BadRequest
  }

  case class NotFoundError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.NotFound
  }
}

