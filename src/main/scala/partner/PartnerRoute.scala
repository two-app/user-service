package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse.ClientError
import user.ConnectCode

class PartnerRoute(partnerService: PartnerService) {
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
    logger.info(s"POST /partner with connect code $connectCode.")
    UserContext.from(request)
      .filterOrElse(ctx => ctx.pid.isEmpty, ClientError("User already has a partner."))
      .map(ctx => ctx.uid)
      .flatMap(uid => ConnectCode.toId(connectCode).map(pid => (uid, pid)).toRight(ClientError("Invalid connect code.")))
      .filterOrElse(ids => ids._1 != ids._2, ClientError("You can't partner with yourself."))
      .map(ids => partnerService.connectUsers(ids._1, ids._2).value)
      .fold(
        e => complete(e.status, e),
        tokensFuture => onSuccess(tokensFuture) {
          case Left(e) => complete(e.status, e)
          case Right(v) => complete(v)
        }
      )
  }
}
