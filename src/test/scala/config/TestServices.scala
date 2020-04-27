package config

import health.HealthDao
import health.DoobieHealthDao
import db.DatabaseTestMixin
import cats.effect.IO
import user.UserDao
import user.DoobieUserDao
import authentication.AuthenticationDao
import authentication.AuthenticationDaoStub
import authentication.AuthenticationServiceDao
import couple.CoupleDao
import couple.DoobieCoupleDao
import partner.PartnerDao
import partner.DoobiePartnerDao
import health.HealthService
import health.HealthServiceImpl
import user.UserService
import user.UserServiceImpl
import partner.PartnerService
import partner.PartnerServiceImpl
import health.HealthRouteDispatcher
import user.SelfRoute
import partner.PartnerRouteDispatcher
import user.UserRouteDispatcher
import cats.effect.ContextShift
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import request.RouteDispatcher

object TestServices extends DatabaseTestMixin {
  // Implicit values required for testing
  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)
  implicit val timer = IO.timer(ExecutionContext.global)

  val healthDao: HealthDao[IO] = new DoobieHealthDao(xa)
  val userDao: UserDao[IO] = new DoobieUserDao(xa)

  /** Real implementation for testing, stub for services **/
  val authDao: AuthenticationDao[IO] = new AuthenticationServiceDao()
  val stubAuthDao: AuthenticationDaoStub[IO] = new AuthenticationDaoStub()

  val coupleDao: CoupleDao[IO] = new DoobieCoupleDao(xa)
  val partnerDao: PartnerDao[IO] = new DoobiePartnerDao(xa)

  val healthService: HealthService[IO] = new HealthServiceImpl(healthDao)
  val userService: UserService[IO] = new UserServiceImpl(userDao, stubAuthDao)
  val partnerService: PartnerService[IO] = new PartnerServiceImpl(
    userService,
    coupleDao,
    stubAuthDao,
    partnerDao
  )

  val healthRouteDispatcher: HealthRouteDispatcher[IO] =
    new HealthRouteDispatcher(healthService)
  val selfRouteDispatcher: SelfRoute[IO] = new SelfRoute(userService)
  val partnerRouteDispatcher: PartnerRouteDispatcher[IO] =
    new PartnerRouteDispatcher(partnerService)
  val userRouteDispatcher: UserRouteDispatcher[IO] = new UserRouteDispatcher(
    userService
  )

  val masterRoute: Route = RouteDispatcher.mergeRoutes(
    healthRouteDispatcher.route,
    selfRouteDispatcher.route,
    partnerRouteDispatcher.route,
    userRouteDispatcher.route
  )
}
