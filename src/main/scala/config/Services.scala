package config

import akka.http.scaladsl.server.Route
import authentication.{AuthenticationDao, AuthenticationServiceDao}
import cats.effect.{Async, ConcurrentEffect, Timer}
import couple.{CoupleDao, DoobieCoupleDao}
import doobie.util.transactor.Transactor
import health._
import partner._
import request.RouteDispatcher
import user._

/**
  * Container for all pure code.
  */
class Services[F[_]: Async: Timer: ConcurrentEffect](xa: Transactor[F]) {

  val healthDao: HealthDao[F] = new DoobieHealthDao(xa)
  val userDao: UserDao[F] = new DoobieUserDao(xa)
  val authDao: AuthenticationDao[F] = new AuthenticationServiceDao()
  val coupleDao: CoupleDao[F] = new DoobieCoupleDao(xa)
  val partnerDao: PartnerDao[F] = new DoobiePartnerDao(xa)

  val healthService: HealthService[F] = new HealthServiceImpl(healthDao)
  val userService: UserService[F] = new UserServiceImpl(userDao, authDao)
  val partnerService: PartnerService[F] = new PartnerServiceImpl(
    userService,
    coupleDao,
    authDao,
    partnerDao
  )

  val healthRouteDispatcher: HealthRouteDispatcher[F] =
    new HealthRouteDispatcher(healthService)

  val selfRoute: SelfRouteDispatcher[F] = new SelfRouteDispatcher(userService)

  val partnerRouteDispatcher: PartnerRouteDispatcher[F] =
    new PartnerRouteDispatcher(partnerService)

  val userRouteDispatcher: UserRouteDispatcher[F] =
    new UserRouteDispatcher(userService)

  val masterRoute: Route = RouteDispatcher.mergeDispatchers(
    healthRouteDispatcher,
    selfRoute,
    partnerRouteDispatcher,
    userRouteDispatcher
  )

}
