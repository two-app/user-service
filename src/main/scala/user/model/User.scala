package user.model

import user.model.ObjectValidationError.InvalidUserError

sealed trait ObjectValidationError

object ObjectValidationError {

  case class InvalidUserError(reason: String) extends ObjectValidationError

}

final case class User(uid: Int, firstName: String, lastName: String)

object User {
  def from(uid: Int, firstName: String, lastName: String): Either[InvalidUserError, User] = {
    if (uid < 1) return Left(InvalidUserError("UID must be greater than zero."))
    if (isEmpty(firstName)) return Left(InvalidUserError("First name must be present."))
    if (isEmpty(lastName)) return Left(InvalidUserError("Last name must be present."))

    Right(new User(uid, firstName, lastName))
  }

  private def isEmpty(s: String): Boolean = Option(s).getOrElse("").isEmpty
}

