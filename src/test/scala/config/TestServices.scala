package config

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
import user.UserService
import user.UserServiceImpl
import partner.PartnerService
import partner.PartnerServiceImpl
import user.SelfRouteDispatcher
import partner.PartnerRouteDispatcher
import user.UserRouteDispatcher
import cats.effect.ContextShift
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import request.RouteDispatcher

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
