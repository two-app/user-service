package response

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsValue, RootJsonFormat, _}

trait ErrorResponse {
  def status: StatusCode

  def reason: String
}

object ErrorResponse {

  implicit object ErrorResponseFormat extends RootJsonFormat[ErrorResponse] {
    override def read(json: JsValue): ErrorResponse = {
      val fields = json.asJsObject.fields
      val status = fields("status").convertTo[Int]
      val reason = fields("reason").toString

      status match {
        case StatusCodes.Unauthorized.intValue => AuthorizationError(reason)
        case StatusCodes.InternalServerError.intValue => InternalError(reason)
        case StatusCodes.BadRequest.intValue => ClientError(reason)
        case StatusCodes.NotFound.intValue => NotFoundError(reason)
      }
    }

    override def write(obj: ErrorResponse): JsValue = new JsObject(fields = Map(
      "status" -> obj.status.toString().toJson,
      "reason" -> obj.reason.toJson)
    )
  }

  final case class AuthorizationError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.Unauthorized
  }

  final case class InternalError(reason: String = "Something went wrong.") extends ErrorResponse {
    override def status: StatusCode = StatusCodes.InternalServerError
  }

  final case class ClientError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.BadRequest
  }

  final case class NotFoundError(reason: String) extends ErrorResponse {
    override def status: StatusCode = StatusCodes.NotFound
  }

}

