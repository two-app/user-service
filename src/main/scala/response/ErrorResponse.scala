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
      val statusCode = ("""\d*""".r findFirstIn fields("status").convertTo[String]).get.toInt
      val reason = fields("reason").convertTo[String]

      StatusCode.int2StatusCode(statusCode) match {
        case StatusCodes.Unauthorized => AuthorizationError(reason)
        case StatusCodes.InternalServerError => InternalError(reason)
        case StatusCodes.BadRequest => ClientError(reason)
        case StatusCodes.NotFound => NotFoundError(reason)
        case _ => InternalError(reason)
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

