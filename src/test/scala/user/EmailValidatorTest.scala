package user

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EmailValidatorTest extends AnyFlatSpec with Matchers {
  "an invalid email" should "fail" in {
    EmailValidator.isValid("random") shouldBe false
  }

  "a valid email" should "succeed" in {
    EmailValidator.isValid("admin@two.com") shouldBe true
  }
}
