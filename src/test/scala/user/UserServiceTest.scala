package user

import java.util.Date

import authentication.{AuthenticationDao, Tokens}
import cats.data.OptionT
import cats.implicits._
import db.DatabaseError
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse.{ClientError, InternalError, NotFoundError}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import java.time.Instant
import org.scalatest.funspec.AnyFunSpec
import cats.effect.IO
import config.MasterRoute
import db.FlywayHelper
import config.Services
import scala.util.Random
import cats.effect.Sync
import response.ErrorResponse
import request.UserContext

import authentication._

class UserRecordMapperTest extends AnyFlatSpec with Matchers {
  "User from UserRecord" should "return the correct data" in {
    val record = UserRecord(
      1,
      Option(2),
      Option(3),
      "admin@two.com",
      "First",
      "Last",
      acceptedTerms = true,
      ofAge = true,
      Instant.now()
    )

    val user: Either[ModelValidationError, User] = UserRecordMapper.from(record)

    user.isRight shouldBe true

    user.map(u => {
      u.uid shouldBe 1
      u.firstName shouldBe "First"
      u.lastName shouldBe "Last"
    })
  }

  "Invalid User from UserRecord" should "return an InvalidUserError" in {
    val record = UserRecord(
      -10,
      Option(2),
      Option(3),
      "admin@two.com",
      "First",
      "Last",
      acceptedTerms = true,
      ofAge = true,
      Instant.now()
    )

    UserRecordMapper.from(record).isLeft shouldBe true
  }

  "User to UserRecord" should "not be implemented" in {
    UserRecordMapper.to(Right(User(1, None, None, "First", "Last"))) shouldBe null
  }
}

class UserServiceTest extends AnyFunSpec with Matchers with BeforeAndAfterEach {

  var userService: UserService[IO] = new UserServiceImpl[IO](
    MasterRoute.services.userDao,
    new AuthenticationDaoStub[IO]()
  )

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("registerUser") {
    it("valid registration should return tokens") {
      val tokens: Tokens =
        userService.registerUser(newUser()).value.unsafeRunSync().right.get

      UserContext.from(tokens.accessToken).right.get.uid should be > 0
    }

    it("should return a client error for a duplicate user") {
      val userRegistration: UserRegistration = newUser()

      userService // register the user once
        .registerUser(userRegistration)
        .value
        .unsafeRunSync()
        .isRight shouldBe true

      val errorOrTokens: Either[ErrorResponse, Tokens] = // register again
        userService.registerUser(userRegistration).value.unsafeRunSync()

      errorOrTokens shouldBe Left(
        ClientError("An account with this email exists.")
      )
    }
  }

  describe("getUser") {
    describe("with uid") {
      it("should return a not found error for a non-existent uid") {
        val uid = 10

        val errorOrUser: Either[ErrorResponse, User] =
          userService.getUser(uid).value.unsafeRunSync()

        errorOrUser shouldBe Left(
          NotFoundError(s"User does not exist.")
        )
      }

      it("should return a user with the registration details") {
        val registration: UserRegistration = newUser()
        val tokens: Tokens =
          userService.registerUser(registration).value.unsafeRunSync().right.get
        val userContext: UserContext =
          UserContext.from(tokens.accessToken).right.get

        val user: User =
          userService.getUser(userContext.uid).value.unsafeRunSync().right.get

        user.firstName shouldBe registration.firstName
        user.lastName shouldBe registration.lastName
        user.pid shouldBe None
        user.cid shouldBe None
      }
    }

    describe("by email") {
      it("should return a not found error for a non-existent uid") {
        val errorOrUser: Either[ErrorResponse, User] =
          userService.getUser("unknown@email.com").value.unsafeRunSync()

        errorOrUser shouldBe Left(
          NotFoundError(s"User does not exist.")
        )
      }

      it("should return a user with the registration details") {
        val registration: UserRegistration = newUser()
        userService.registerUser(registration).value.unsafeRunSync().right.get

        val email: String = registration.email
        val user: User =
          userService.getUser(email).value.unsafeRunSync().right.get

        user.firstName shouldBe registration.firstName
        user.lastName shouldBe registration.lastName
        user.pid shouldBe None
        user.cid shouldBe None
      }
    }
  }

  def newUser(): UserRegistration =
    UserRegistration
      .from(
        "First",
        "Last",
        randomEmail(),
        "TestPassword",
        acceptedTerms = true,
        ofAge = true
      )
      .right
      .get

  def randomEmail(): String =
    "quillCoupleTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"
}
