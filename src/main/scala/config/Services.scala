package config

import cats.effect.Sync
import cats.effect.Async
import cats.effect.ContextShift

import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, DoobieCoupleDao}
import doobie.util.transactor.Transactor.Aux
import partner.{PartnerRoute, PartnerService, PartnerServiceImpl}
import user._

/**
  * Container for all pure code.
  */
class Services[F[_]: Sync: Async: ContextShift] {
  lazy val partnerService: PartnerService[F] =
    new PartnerServiceImpl[F](userService, coupleDao, authDao)
  lazy val userService: UserService[F] =
    new UserServiceImpl[F](userDao, authDao)

  lazy val userDao: UserDao[F] = new DoobieUserDao[F](transactor)
  lazy val authDao: AuthenticationDao[F] = new AuthenticationServiceDao[F]()
  lazy val coupleDao: CoupleDao[F] = new DoobieCoupleDao[F](transactor)

  lazy val transactor: Aux[F, Unit] = db.transactor[F]()
}
