package config

import akka.http.scaladsl.server.Route
import authentication.{
  AuthenticationDao,
  AuthenticationDaoStub,
  AuthenticationServiceDao
}
import cats.effect.IO
import couple.{CoupleDao, DoobieCoupleDao}
import db.DatabaseTestMixin
import partner._
import request.RouteDispatcher
import user._

object TestServices extends DatabaseTestMixin {
  val userDao: UserDao[IO] = new DoobieUserDao(xa)

  /** Real implementation for testing, stub for services **/
  val authDao: AuthenticationDao[IO] = new AuthenticationServiceDao()
  val stubAuthDao: AuthenticationDaoStub[IO] = new AuthenticationDaoStub()

  val coupleDao: CoupleDao[IO] = new DoobieCoupleDao(xa)
  val partnerDao: PartnerDao[IO] = new DoobiePartnerDao(xa)

  val userService: UserService[IO] = new UserServiceImpl(userDao, stubAuthDao)
  val partnerService: PartnerService[IO] = new PartnerServiceImpl(
    userService,
    coupleDao,
    stubAuthDao,
    partnerDao
  )

  val selfRouteDispatcher: SelfRouteDispatcher[IO] = new SelfRouteDispatcher(
    userService
  )
  val partnerRouteDispatcher: PartnerRouteDispatcher[IO] =
    new PartnerRouteDispatcher(partnerService)
  val userRouteDispatcher: UserRouteDispatcher[IO] = new UserRouteDispatcher(
    userService
  )

  val masterRoute: Route = RouteDispatcher.mergeRoutes(
    selfRouteDispatcher.route,
    partnerRouteDispatcher.route,
    userRouteDispatcher.route
  )
}
