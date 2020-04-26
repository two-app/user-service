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

class HealthRouteDispatcher(healthService: HealthService[IO])
    extends RouteDispatcher {

  override def route: Route = path("health") {
    get {
      handleHealthGet()
    }
  }

  def handleHealthGet(): Route = {
    val healthEffect = healthService.getHealth().leftWiden[ErrorResponse]
    onSuccess(healthEffect.value.unsafeToFuture()) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(_) => complete(StatusCodes.OK)
    }
  }

}
