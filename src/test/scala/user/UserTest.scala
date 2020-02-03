package user

import org.scalatest.flatspec.AnyFlatSpec

class UserTest extends AnyFlatSpec {

  "A valid user" should "return the user" in {
    assert(User.from(1, "First", "Last") == Right(User(1, "First", "Last")))
  }

  "A UID of zero" should "return InvalidUserError" in {
    assert(User.from(0, "Two", "Two") == Left(InvalidUserError("UID must be greater than zero.")))
  }

  "An empty First Name" should "return InvalidUserError" in {
    assert(User.from(1, "", "Two") == Left(InvalidUserError("First name must be present.")))
  }

  "A null First Name" should "return InvalidUserError" in {
    assert(User.from(1, null, "Two") == Left(InvalidUserError("First name must be present.")))
  }

  "An empty Last Name" should "return InvalidUserError" in {
    assert(User.from(1, "Two", "") == Left(InvalidUserError("Last name must be present.")))
  }

  "A null Last Name" should "return InvalidUserError" in {
    assert(User.from(1, "Two", null) == Left(InvalidUserError("Last name must be present.")))
  }
}
