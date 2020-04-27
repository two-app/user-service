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

  val healthDao: HealthDao[F] = new DoobieHealthDao[F](xa)
  val userDao: UserDao[F] = new DoobieUserDao[F](xa)
  val authDao: AuthenticationDao[F] = new AuthenticationServiceDao[F]()
  val coupleDao: CoupleDao[F] = new DoobieCoupleDao[F](xa)
  val partnerDao: PartnerDao[F] = new DoobiePartnerDao[F](xa)

  val healthService: HealthService[F] = new HealthServiceImpl[F](healthDao)
  val userService: UserService[F] = new UserServiceImpl[F](userDao, authDao)
  val partnerService: PartnerService[F] = new PartnerServiceImpl[F](
    userService,
    coupleDao,
    authDao,
    partnerDao
  )

  val healthRouteDispatcher: HealthRouteDispatcher[F] =
    new HealthRouteDispatcher[F](healthService)

  val selfRoute: SelfRoute[F] = new SelfRoute[F](userService)

  val partnerRouteDispatcher: PartnerRouteDispatcher[F] =
    new PartnerRouteDispatcher[F](partnerService)

  val userRouteDispatcher: UserRouteDispatcher[F] =
    new UserRouteDispatcher[F](userService)

  val masterRoute: Route = RouteDispatcher.mergeRoutes(
    healthRouteDispatcher.route,
    selfRoute.route,
    partnerRouteDispatcher.route,
    userRouteDispatcher.route
  )

}
