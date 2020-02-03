package user

import java.util.Date

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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

}
