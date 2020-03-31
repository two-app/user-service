package config

import akka.http.scaladsl.server.Route
import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import partner.{PartnerRoute, PartnerService, PartnerServiceImpl}
import user._
import cats.effect.ContextShift
import doobie.util.ExecutionContexts
import cats.effect.IO

object MasterRoute {
  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)

  lazy val userRoute: Route = new UserRoute(services.userService).route
  lazy val partnerRoute: Route = new PartnerRoute(services.partnerService).route

  lazy val services: Services[IO] = new Services[IO]()
}
