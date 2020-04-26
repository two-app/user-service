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

class HealthRouteDispatcher(healthService: HealthService[IO])
    extends RouteDispatcher {

  val logger: Logger = Logger[HealthRouteDispatcher]

  override def route: Route = path("health") {
    get {
      handleHealthGet()
    }
  }

  def handleHealthGet(): Route = {
    logger.info("GET /health")
    logger.info("Performing health check.")
    val healthEffect = healthService.getHealth().leftWiden[ErrorResponse]
    onSuccess(healthEffect.value.unsafeToFuture()) {
      case Left(error: ErrorResponse) =>
        logger.error("Health check failed. Responding with internal server error.")
        complete(error.status, error)
      case Right(_) =>
        logger.info("Health check completed. All OK.")
        complete(StatusCodes.OK)

    }
  }

}
