package config

import akka.http.scaladsl.server.Route
import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import partner.{PartnerRoute, PartnerService, PartnerServiceImpl}
import user.SelfRoute
import user.UserRouteDispatcher
import cats.effect.ContextShift
import doobie.util.ExecutionContexts
import cats.effect.IO
import request.RouteDispatcher

object MasterRoute {
  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)

  val services: Services[IO] = new Services[IO]()

  val selfRoute: Route = new SelfRoute(services.userService).route
  val partnerRoute: Route = new PartnerRoute(services.partnerService).route
  val userRoute: Route = new UserRouteDispatcher(services.userService).route

  val masterRoute: Route = RouteDispatcher.mergeRoutes(selfRoute, partnerRoute, userRoute)
}
