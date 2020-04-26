package health

import akka.http.scaladsl.server.Route
import cats.effect.IO
import request.RouteDispatcher

class HealthRouteDispatcher(healthService: HealthService[IO])
    extends RouteDispatcher {

  override def route: Route = ???

}
