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
import cats.effect.ConcurrentEffect
import cats.effect.implicits._

class PartnerRouteDispatcher[F[_]: ConcurrentEffect](partnerService: PartnerService[F]) {
  val logger: Logger = Logger[PartnerRoute[F]]
  val partnerRoute: PartnerRoute[F] = new PartnerRoute[F](partnerService)

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
      partnerRoute
        .getPartner(request)
        .value
        .toIO
        .unsafeToFuture()

    onSuccess(futureResponse) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(user) => complete(user)
    }
  }

  def connectUserToPartner(request: HttpRequest, connectCode: String): Route = {
    logger.info(s"POST /partner with connect code $connectCode.")
    val tokensFuture = partnerRoute.connectUsers(request, connectCode)
      .value
      .toIO
      .unsafeToFuture()

    onSuccess(tokensFuture) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(tokens: Tokens)      => complete(tokens)
    }
  }
}

class PartnerRoute[F[_]: Monad](partnerService: PartnerService[F]) {
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

  def connectUsers(
      request: HttpRequest,
      connectCode: String
  ): EitherT[F, ErrorResponse, Tokens] = {
    for {
      ctx <- this.extractContext(request)
      pid <- this.extractPid(ctx, connectCode)
      tokens <- partnerService.connectUsers(ctx.uid, pid)
    } yield tokens
  }

  private def extractContext(
      request: HttpRequest
  ): EitherT[F, ErrorResponse, UserContext] =
    UserContext
      .from(request)
      .filterOrElse(
        ctx => ctx.pid.isEmpty,
        ClientError("User already has a partner.")
      )
      .toEitherT[F]
      .leftWiden[ErrorResponse]

  private def extractPid(
      ctx: UserContext,
      connectCode: String
  ): EitherT[F, ErrorResponse, Int] =
    ConnectCode
      .toId(connectCode)
      .toRight(ClientError("Invalid connect code."))
      .filterOrElse(
        pid => pid != ctx.uid,
        ClientError("You can't partner with yourself.")
      )
      .toEitherT[F]
      .leftWiden[ErrorResponse]
}
