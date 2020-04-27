package config

import cats.effect.Sync
import cats.effect.Async
import cats.effect.ContextShift

import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import partner.{PartnerRouteDispatcher, PartnerService, PartnerServiceImpl}
import user._
import partner.PartnerDao
import partner.DoobiePartnerDao
import health.HealthDao
import health.DoobieHealthDao
import health.HealthService
import health.HealthServiceImpl
import doobie.util.transactor.Transactor
import health.HealthRouteDispatcher
import cats.effect.Timer
import cats.effect.ConcurrentEffect
import akka.http.scaladsl.server.Route
import request.RouteDispatcher

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
