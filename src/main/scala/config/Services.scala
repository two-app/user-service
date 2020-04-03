package config

import cats.effect.Sync
import cats.effect.Async
import cats.effect.ContextShift

import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import doobie.util.transactor.Transactor.Aux
import partner.{PartnerRoute, PartnerService, PartnerServiceImpl}
import user._
import partner.PartnerDao
import partner.DoobiePartnerDao

/**
  * Container for all pure code.
  */
class Services[F[_]: Sync: Async: ContextShift] {
  lazy val partnerService: PartnerService[F] =
    new PartnerServiceImpl[F](userService, coupleDao, authDao, partnerDao)
  lazy val userService: UserService[F] =
    new UserServiceImpl[F](userDao, authDao)

  lazy val userDao: UserDao[F] = new DoobieUserDao[F](xa)
  lazy val authDao: AuthenticationDao[F] = new AuthenticationServiceDao[F]()
  lazy val coupleDao: CoupleDao[F] = new DoobieCoupleDao[F](xa)
  lazy val partnerDao: PartnerDao[F] = new DoobiePartnerDao[F](xa)

  lazy val xa: Aux[F, Unit] = db.transactor[F]()
}
