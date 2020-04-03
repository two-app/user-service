package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse.ClientError
import user.ConnectCode
import cats.effect.IO
import cats.data.EitherT
import cats.implicits._
import response.ErrorResponse
import authentication.Tokens
import cats.data.OptionT
import user.User
import response.ErrorResponse.NotFoundError
import cats.Monad
import scala.concurrent.Future

class PartnerRoute(partnerService: PartnerService[IO]) {
  val logger: Logger = Logger(classOf[PartnerRoute])
  val partnerRouteI: PartnerRouteI[IO] = new PartnerRouteI[IO](partnerService)

  val route: Route = extractRequest { request =>
    concat(
      path("partner") {
        get {
          handleGetPartner(request)
        }
      },
      path("partner" / Segment) { connectCode =>
        post {
          connectUserToPartner(request, connectCode)
        }
      }
    )
  }

  /**
    * The purpose of this route is two fold,
    * 1. the user may be unconnected and refreshing
    * 2. the user is connected and requesting their partner.
    * Therefor we cannot rely on the PID being present.
   **/
  def handleGetPartner(request: HttpRequest): Route = {
    val futureResponse: Future[Either[ErrorResponse, User]] =
      partnerRouteI.getPartner(request).value.unsafeToFuture()

    onSuccess(futureResponse) {
      case Left(error) => complete(error.status, error)
      case Right(user) => complete(user)
    }
  }

  def connectUserToPartner(request: HttpRequest, connectCode: String): Route = {
    logger.info(s"POST /partner with connect code $connectCode.")
    UserContext
      .from(request)
      .filterOrElse(
        ctx => ctx.pid.isEmpty,
        ClientError("User already has a partner.")
      )
      .map(ctx => ctx.uid)
      .flatMap(uid =>
        ConnectCode
          .toId(connectCode)
          .map(pid => (uid, pid))
          .toRight(ClientError("Invalid connect code."))
      )
      .filterOrElse(
        ids => ids._1 != ids._2,
        ClientError("You can't partner with yourself.")
      )
      .map(ids => partnerService.connectUsers(ids._1, ids._2).value)
      .fold(
        e => complete(e.status, e),
        tokensEffect =>
          onSuccess(tokensEffect.unsafeToFuture()) {
            case Left(e)  => complete(e.status, e)
            case Right(v) => complete(v)
          }
      )
  }
}

// TODO move to Dispatch pattern
class PartnerRouteI[F[_]: Monad](partnerService: PartnerService[F]) {
  def getPartner(
      request: HttpRequest
  ): EitherT[F, ErrorResponse, User] =
    EitherT
      .fromEither[F](UserContext.from(request))
      .flatMap(user =>
        partnerService
          .getPartner(user.uid)
          .toRight(NotFoundError("You haven't connected with a partner yet."))
      )
}
