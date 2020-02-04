package user

import java.util.Date

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse.{ClientError, NotFoundError}

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
  "valid user record" should "be mapped to a user" in {
    val record = UserRecord(1, None, None, "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, null)
    val service = new UserServiceImpl((_: Int) => Future(Option(record)))

    service.getUser(1).map(_ shouldBe Right(User(1, "First", "Last")))
  }

  "empty user record" should "return a NotFound error" in {
    val service = new UserServiceImpl((_: Int) => Future(None))

    service.getUser(1).map(_ shouldBe Left(NotFoundError(s"User with UID 1 does not exist.")))
  }

  "invalid user record" should "return a Client error" in {
    val record = UserRecord(-1, None, None, "admin@two.com", "First", "Last", acceptedTerms = true, ofAge = true, null)
    val service = new UserServiceImpl((_: Int) => Future(Option(record)))
    val expectedError = ClientError("User record malformed. Reason: UID must be greater than zero.")

    service.getUser(1).map(_ shouldBe Left(expectedError))
  }
}
