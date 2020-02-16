package user

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserRegistrationTest extends AnyFlatSpec with Matchers {
  val validRegistration: UserRegistration = UserRegistration("First", "Last", "user@two.com", "Passw0rd", acceptedTerms = true, ofAge = true)

  "an empty first name" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("", "", "", "", acceptedTerms = true, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("First name must be present."))
  }

  "an empty last name" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "", "", "", acceptedTerms = true, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("Last name must be present."))
  }

  "an empty email" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "Last", "", "", acceptedTerms = true, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("Email must be valid."))
  }

  "an invalid email" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "Last", "bla", "", acceptedTerms = true, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("Email must be valid."))
  }

  "a short password" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "Last", "user@two.com", "short", acceptedTerms = true, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("Password must be at least six characters in length."))
  }

  "unaccepted terms" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "Last", "user@two.com", "validpassword", acceptedTerms = false, ofAge = true)
    errorOrRegistration shouldBe Left(ModelValidationError("Terms and Conditions must be accepted."))
  }

  "unaccepted age requirement" should "return a validation error" in {
    val errorOrRegistration = UserRegistration.from("First", "Last", "user@two.com", "validpassword", acceptedTerms = true, ofAge = false)
    errorOrRegistration shouldBe Left(ModelValidationError("You must meet the age requirements."))
  }
}

class UserTest extends AnyFlatSpec with Matchers{

  "A valid user" should "return the user" in {
    User.from(1, Option(2), Option(3), "First", "Last") shouldBe Right(User(1, Option(2), Option(3), "First", "Last"))
  }

  "A UID of zero" should "return a validation error" in {
    User.from(0, None, None, "Two", "Two") shouldBe Left(ModelValidationError("UID must be greater than zero."))
  }

  "An empty First Name" should "return a validation error" in {
    User.from(1, None, None, "", "Two") shouldBe Left(ModelValidationError("First name must be present."))
  }

  "A null First Name" should "return a validation error" in {
    User.from(1, None, None, null, "Two") shouldBe Left(ModelValidationError("First name must be present."))
  }

  "An empty Last Name" should "return a validation error" in {
    User.from(1, None, None, "Two", "") shouldBe Left(ModelValidationError("Last name must be present."))
  }

  "A null Last Name" should "return a validation error" in {
    User.from(1, None, None, "Two", null) shouldBe Left(ModelValidationError("Last name must be present."))
  }
}
