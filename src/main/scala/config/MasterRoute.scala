package config

import akka.http.scaladsl.server.Route
import authentication.{AuthenticationDao, AuthenticationServiceDao}
import couple.{CoupleDao, QuillCoupleDao}
import partner.{PartnerRoute, PartnerService}
import user._

object MasterRoute {
  lazy val userRoute: Route = new UserRoute(userService).route
  lazy val partnerRoute: Route = new PartnerRoute(partnerService).route

  lazy val partnerService = new PartnerService(userService, coupleDao)
  lazy val userService: UserService = new UserServiceImpl(userDao, authDao)
  lazy val userDao: UserDao = new QuillUserDao
  lazy val authDao: AuthenticationDao = new AuthenticationServiceDao
  lazy val coupleDao: CoupleDao = new QuillCoupleDao
}
