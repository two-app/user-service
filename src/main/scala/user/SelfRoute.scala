package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import authentication.Tokens
import cats.effect.IO
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}
import cats.effect.ConcurrentEffect
import cats.effect.implicits._
import cats.implicits._
import scala.concurrent.Future
import cats.data.EitherT
import response.ErrorResponse.InternalError
import request.RouteDispatcher

class SelfRouteDispatcher[F[_]: ConcurrentEffect](userService: UserService[F])
    extends RouteDispatcher {
  implicit val logger: Logger = Logger[SelfRouteDispatcher[F]]

  override val route: Route = extractRequest { request =>
    path("self") {
      concat(
        get {
          getSelf(request)
        },
        post {
          entity(as[Either[ModelValidationError, UserRegistration]]) { entity =>
            postSelf(request, entity)
          }
        }
      )
    }
  }

  def postSelf(
      request: HttpRequest,
      entity: Either[ModelValidationError, UserRegistration]
  ): Route = {
    logger.info("POST /self")
    val tokensEffect = for {
      registration <- entity.toEitherT[F].leftMap(mve => ClientError(mve.reason))
      tokens <- userService.registerUser(registration)
    } yield tokens

    completeEffectfulEither(tokensEffect)
  }

  def getSelf(request: HttpRequest): Route = {
    logger.info("GET /self")

    val userEffect = for {
      ctx <- EitherT.fromEither[F](UserContext.from(request))
      user <- userService.getUser(ctx.uid).leftMap {
        case NotFoundError(e) => InternalError()
        case x                => x
      }
    } yield user

    completeEffectfulEither(userEffect)
  }
}
