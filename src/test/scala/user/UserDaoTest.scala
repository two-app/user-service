package user

import cats.implicits._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AsyncFunSpec
import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import db._
import java.time.Instant

class QuillUserDaoTest
    extends AsyncFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val xa: Aux[IO, Unit] = TransactorUtil.transactor()
  val userDao: UserDao[IO] = new DoobieUserDao[IO](xa)

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("storeUser") {
    it("should generate unique UIDs") {
      val registrations: List[UserRegistration] =
        (1 to 5).toList.map(i => newUserRegistration(s"user$i@two.com"))

      val uids: List[Int] = registrations.map(registration =>
        userDao.storeUser(registration).value.unsafeRunSync().right.get
      )

      uids shouldBe (1 to 5)
    }

    it("should return a DuplicateRecordError with an existing email") {
      val registration: UserRegistration = newUserRegistration("unique@two.com")

      val maybeError: Either[DatabaseError, Int] =
        userDao.storeUser(registration).value.unsafeRunSync()
      maybeError.isRight shouldBe true

      val shouldBeError: Either[DatabaseError, Int] =
        userDao.storeUser(registration).value.unsafeRunSync()

      shouldBeError shouldBe Left(DuplicateRecordError)
    }
  }

  describe("getUser") {
    it("should have the same user content") {
      val registration: UserRegistration = newUserRegistration("user@two.com")

      val uid = userDao.storeUser(registration).value.unsafeRunSync().right.get
      val storedUser: UserRecord =
        userDao.getUser(uid).value.unsafeRunSync().get

      storedUser.uid shouldBe uid
      storedUser.cid shouldBe None
      storedUser.pid shouldBe None
      storedUser.email shouldBe registration.email
      storedUser.firstName shouldBe registration.firstName
      storedUser.lastName shouldBe registration.lastName
      storedUser.acceptedTerms shouldBe true
      storedUser.ofAge shouldBe true
    }
  }

  def newUserRegistration(email: String): UserRegistration = UserRegistration(
    firstName = "First",
    lastName = "Last",
    email = email,
    acceptedTerms = true,
    ofAge = true,
    password = "SaFePassW0rD"
  )
}
