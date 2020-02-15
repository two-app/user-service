package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse.ClientError

class PartnerRoute {
  val logger: Logger = Logger(classOf[PartnerRoute])

  val route: Route = post {
    path("partner" / Segment) {
      connectCode => {
        extractRequest {
          request => connectUserToPartner(request, connectCode)
        }
      }
    }
  }

  def connectUserToPartner(request: HttpRequest, connectCode: String): Route = {
    UserContext.from(request)
      .filterOrElse(uc => uc.connectCode.isDefined, ClientError("User already has a partner."))
      .map(_ => complete("ok"))
      .fold(e => complete(e.status, e), v => v)
  }
}
