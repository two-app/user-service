package partner

import authentication.{AuthenticationDao, Tokens}
import cats.data.EitherT
import cats.implicits._
import couple.{CoupleDao, CoupleRecord}
import config.TestServices
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}
import user.{User, UserRegistration, UserService}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import db.DatabaseTestMixin
import cats.effect.IO
import authentication.AuthenticationDaoStub
import scala.util.Random
import request.UserContext
import user.UserServiceImpl
import user.UserTestArbitraries

class PartnerServiceTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach
    with UserTestArbitraries
    with DatabaseTestMixin {

  val userService: UserService[IO] = TestServices.userService
  val partnerService: PartnerService[IO] = TestServices.partnerService

  override def beforeEach(): Unit = cleanMigrate()

  def registerUser(): UserContext =
    userService
      .registerUser(randomUserRegistration())
      .value
      .unsafeRunSync()
      .map(tokens => UserContext.from(tokens.accessToken).right.get)
      .right
      .get

  def connect(uid: Int, pid: Int): UserContext =
    partnerService
      .connectUsers(uid, pid)
      .value
      .unsafeRunSync()
      .map(tokens => UserContext.from(tokens.accessToken).right.get)
      .right
      .get

  describe("connectUsers") {
    it("should generate tokens for a valid partnership") {
      val userOneId: Int = registerUser().uid
      val userTwoId: Int = registerUser().uid

      val newUserContext: UserContext = connect(userOneId, userTwoId)

      newUserContext.uid shouldBe userOneId
      newUserContext.pid shouldBe Option(userTwoId)
      newUserContext.cid shouldBe Option(1)
    }

    it("should return a Not Found error if the partner does not exist") {
      val userOneId: Int = registerUser().uid

      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userOneId, 100)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(NotFoundError("Partner does not exist."))
    }

    it("should return a client error if the user is already connected") {
      val userOneId: Int = registerUser().uid
      val userTwoId: Int = registerUser().uid

      connect(userOneId, userTwoId)

      val userThreeId: Int = registerUser().uid
      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userOneId, userThreeId)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("User already has a partner."))
    }

    it("should return a client error if the partner is already connected") {
      val userOneId: Int = registerUser().uid
      val userTwoId: Int = registerUser().uid

      connect(userOneId, userTwoId)

      val userThreeId: Int = registerUser().uid
      val errorOrTokens: Either[ErrorResponse, Tokens] = partnerService
        .connectUsers(userThreeId, userOneId)
        .value
        .unsafeRunSync()

      errorOrTokens shouldBe Left(ClientError("Partner already has a partner."))
    }
  }

  describe("getPartner") {
    describe("Connected user") {
      it("should return the partner") {
        val user = registerUser()
        val partner = registerUser()

        val cid = connect(user.uid, partner.uid).cid.get

        val maybePartner =
          partnerService.getPartner(user.uid).value.unsafeRunSync()

        maybePartner shouldBe Some(
          User(partner.uid, Option(user.uid), Option(cid), "First", "Last")
        )
      }
    }

    describe("Unconnected user") {
      it("should return None") {
        val user = registerUser()

        val maybePartner =
          partnerService.getPartner(user.uid).value.unsafeRunSync()

        maybePartner shouldBe None
      }
    }
  }
}
