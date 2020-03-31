package partner

import authentication.{AuthenticationDao, Tokens}
import cats.data.EitherT
import cats.implicits._
import couple.{CoupleDao, CoupleRecord}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}
import user.{User, UserRegistration, UserService}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import db.FlywayHelper
import cats.effect.IO
import config.MasterRoute
import authentication.AuthenticationDaoStub
import scala.util.Random
import request.UserContext
import user.UserServiceImpl

class PartnerServiceImplTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val userService: UserService[IO] = new UserServiceImpl[IO](
    MasterRoute.services.userDao,
    new AuthenticationDaoStub()
  )

  val partnerService: PartnerService[IO] = new PartnerServiceImpl[IO](
    userService,
    MasterRoute.services.coupleDao,
    new AuthenticationDaoStub()
  )

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("connectUsers") {
    def register(registration: UserRegistration): Int = {
      userService
        .registerUser(registration)
        .value
        .unsafeRunSync()
        .map(tokens => UserContext.from(tokens.accessToken).right.get)
        .right
        .get
        .uid
    }

    def connect(uid: Int, pid: Int): Tokens = {
      partnerService
        .connectUsers(uid, pid)
        .value
        .unsafeRunSync()
        .right
        .get
    }

    it("should generate tokens for a valid partnership") {
      val userOneId: Int = register(newUser())
      val userTwoId: Int = register(newUser())

      val updatedTokens: Tokens = connect(userOneId, userTwoId)

      val newUserContext: UserContext =
        UserContext.from(updatedTokens.accessToken).right.get

      newUserContext.uid shouldBe userOneId
      newUserContext.pid shouldBe Option(userTwoId)
      newUserContext.cid shouldBe Option(1)
    }

    it("should return a Not Found error if the partner does not exist") {
      val userOneId: Int = register(newUser())

      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userOneId, 100)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(NotFoundError("Partner does not exist."))
    }

    it("should return a client error if the user is already connected") {
      val userOneId: Int = register(newUser())
      val userTwoId: Int = register(newUser())

      connect(userOneId, userTwoId)

      val userThreeId: Int = register(newUser())
      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userOneId, userThreeId)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("User already has a partner."))
    }

    it("should return a client error if the partner is already connected") {
      val userOneId: Int = register(newUser())
      val userTwoId: Int = register(newUser())

      connect(userOneId, userTwoId)

      val userThreeId: Int = register(newUser())
      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userThreeId, userOneId)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("Partner already has a partner."))
    }
  }

  def newUser(): UserRegistration = UserRegistration(
    firstName = "First",
    lastName = "Last",
    email = randomEmail(),
    password = "StrongPassword",
    acceptedTerms = true,
    ofAge = true
  )

  def randomEmail(): String =
    "userServiceWorkflowTest-" + Random.alphanumeric
      .take(10)
      .mkString + "@twotest.com"

  // "if the partner does not exist it" should "return a not found error" in {
  //   userServiceStub.userMap.put(1, User(1, None, None, "First", "Last"))

  //   partnerService.connectUsers(1, 10).map(errorOrTokens => {
  //     errorOrTokens shouldBe Left(NotFoundError(""))
  //   })
  // }

  // "if the partner already has a partner it" should "return a client error" in {
  //   userServiceStub.userMap.put(1, User(1, None, None, "User", "Last"))
  //   userServiceStub.userMap.put(10, User(10, Option(2), Option(3), "Partner", "Last"))

  //   partnerService.connectUsers(1, 10).map(errorOrTokens => {
  //     errorOrTokens shouldBe Left(ClientError("Partner already has a partner."))
  //   })
  // }

}
