package user

import java.util.Date

import db.DatabaseError
import db.DatabaseError.{DuplicateEntry, Other}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse.{ClientError, InternalError, NotFoundError}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

class UserRecordMapperTest extends AnyFlatSpec with Matchers {
  "User from UserRecord" should "return the correct data" in {
    val date = new Date()
    val record = UserRecord(1, Option(2), Option(3), "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, date)

    val user: Either[InvalidUserError, User] = UserRecordMapper.from(record)

    user.isRight shouldBe true

    user.map(u => {
      u.uid shouldBe 1
      u.firstName shouldBe "First"
      u.lastName shouldBe "Last"
    })
  }

  "Invalid User from UserRecord" should "return an InvalidUserError" in {
    val date = new Date()
    val record = UserRecord(-10, Option(2), Option(3), "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, date)

    UserRecordMapper.from(record).isLeft shouldBe true
  }

  "User to UserRecord" should "not be implemented" in {
    UserRecordMapper.to(Right(User(1, "First", "Last"))) shouldBe null
  }
}

class UserServiceTest extends AnyFlatSpec with Matchers {

  def storeUserDaoStub(storeUserResult: Either[DatabaseError, Int]): UserDao = new UserDao {
    override def storeUser(ur: UserRegistration): Future[Either[DatabaseError, Int]] = Future(storeUserResult)

    override def getUser(uid: Int): Future[Option[UserRecord]] = null
  }

  val testRegistration: UserRegistration = UserRegistration("First", "Last", "test@two.com", "Passw0rd", acceptedTerms = true, ofAge = true)

  "valid user registration" should "return the new uid" in {
    new UserServiceImpl(storeUserDaoStub(Right(12))).registerUser(testRegistration).map(errorOrUid => {
      errorOrUid.isRight shouldBe true
      errorOrUid.right.get shouldBe 12
    })
  }

  "duplicate user registration" should "return Client Error" in {
    new UserServiceImpl(storeUserDaoStub(Left(DuplicateEntry()))).registerUser(testRegistration).map(errorOrUid => {
      errorOrUid.isLeft shouldBe true
      errorOrUid.left shouldBe ClientError("An account with this email exists.")
    })
  }

  "an unknown error" should "be mapped to an Internal Error" in {
    new UserServiceImpl(storeUserDaoStub(Left(Other()))).registerUser(testRegistration).map(errorOrUid => {
      errorOrUid.isLeft shouldBe true
      errorOrUid.left shouldBe InternalError()
    })
  }

  def getUserDaoStub(getUserResult: Option[UserRecord]): UserDao = new UserDao {
    override def storeUser(ur: UserRegistration): Future[Either[DatabaseError, Int]] = null

    override def getUser(uid: Int): Future[Option[UserRecord]] = Future(getUserResult)
  }

  "valid user record" should "be mapped to a user" in {
    val record = UserRecord(1, None, None, "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, null)
    val service = new UserServiceImpl(getUserDaoStub(Option(record)))

    service.getUser(1).map(_ shouldBe Right(User(1, "First", "Last")))
  }

  "empty user record" should "return a NotFound error" in {
    val service = new UserServiceImpl(getUserDaoStub(None))

    service.getUser(1).map(_ shouldBe Left(NotFoundError(s"User with UID 1 does not exist.")))
  }

  "invalid user record" should "return a Client error" in {
    val record = UserRecord(-1, None, None, "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, null)
    val service = new UserServiceImpl(getUserDaoStub(Option(record)))
    val expectedError = ClientError("User record malformed. Reason: UID must be greater than zero.")

    service.getUser(1).map(_ shouldBe Left(expectedError))
  }

}
