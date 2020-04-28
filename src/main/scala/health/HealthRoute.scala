package health

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.implicits._
import request.RouteDispatcher
import response.ErrorResponse.InternalError
import response.ErrorResponse
import com.typesafe.scalalogging.Logger
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration._
import cats.effect.Timer
import cats.effect.ConcurrentEffect
import cats.Monad
import cats.effect.implicits._

class HealthRouteDispatcher[F[_]: Timer : ConcurrentEffect : Monad](healthService: HealthService[F])
    extends RouteDispatcher {

  val logger: Logger = Logger[HealthRouteDispatcher[F]]

  override def route: Route = path("health") {
    get {
      handleHealthGet()
    }
  }

  def handleHealthGet(): Route = {
    logger.info("GET /health - Performing health check.")
    val healthFuture = healthService.getHealth().leftWiden[ErrorResponse]
      .value
      .timeout(3.seconds)
      .toIO
      .unsafeToFuture()

    onComplete(healthFuture) {
      case Failure(exception) => 
        handleControlledError(Left(InternalError()))
      case Success(maybeError) =>
        handleControlledError(maybeError)
    }
  }

  private def handleControlledError(maybeError: Either[ErrorResponse, Any]): Route = {
    maybeError match {
      case Left(error: ErrorResponse) =>
        logger.error("Health check failed. Responding with internal server error.")
        complete(error.status, error)
      case Right(_) =>
        logger.info("Health check completed. All OK.")
        complete(StatusCodes.OK)
    }
  }

}
