package config

import cats.effect.Sync
import cats.effect.Async
import cats.effect.ContextShift

import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import partner.{PartnerRoute, PartnerService, PartnerServiceImpl}
import user._
import partner.PartnerDao
import partner.DoobiePartnerDao
import health.HealthDao
import health.DoobieHealthDao
import health.HealthService
import health.HealthServiceImpl
import doobie.util.transactor.Transactor

/**
  * Container for all pure code.
  */
class Services[F[_]: Async](xa: Transactor[F]) {

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

}
