package user

import spray.json.DefaultJsonProtocol.{jsonFormat3, _}
import spray.json.{JsBoolean, JsString, JsValue, RootJsonFormat}

final case class ModelValidationError(reason: String)

case class UserRegistration
(
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  acceptedTerms: Boolean,
  ofAge: Boolean
)

object UserRegistration {

  implicit object UserRegistrationFormat extends RootJsonFormat[Either[ModelValidationError, UserRegistration]] {
    override def write(obj: Either[ModelValidationError, UserRegistration]): JsValue = null

    override def read(json: JsValue): Either[ModelValidationError, UserRegistration] = {
      val f = json.asJsObject.fields
      UserRegistration.from(
        firstName = extractString(f, "firstName"),
        lastName = extractString(f, "lastName"),
        email = extractString(f, "email"),
        password = extractString(f, "password"),
        acceptedTerms = extractBool(f, "acceptedTerms"),
        ofAge = extractBool(f, "ofAge")
      )
    }

    def extractString(f: Map[String, JsValue], k: String): String = f.getOrElse(k, JsString.empty).convertTo[String]

    def extractBool(f: Map[String, JsValue], k: String): Boolean = f.getOrElse(k, JsBoolean(false)).convertTo[Boolean]
  }

  def from(firstName: String, lastName: String, email: String, password: String, acceptedTerms: Boolean, ofAge: Boolean)
  : Either[ModelValidationError, UserRegistration] = {
    if (firstName.length < 2) return fail("First name must be present.")
    if (lastName.length < 2) return fail("Last name must be present.")
    if (EmailValidator.isInvalid(email)) return fail("Email must be valid.")
    if (password.length < 6) return fail("Password must be at least six characters in length.")
    if (!acceptedTerms) return fail("Terms and Conditions must be accepted.")
    if (!ofAge) return fail("You must meet the age requirements.")

    Right(new UserRegistration(firstName, lastName, email, password, acceptedTerms, ofAge))
  }

  private def fail(reason: String): Either[ModelValidationError, UserRegistration] = Left(ModelValidationError(reason))
}

final case class User(uid: Int, firstName: String, lastName: String)

object User {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)

  def from(uid: Int, firstName: String, lastName: String): Either[ModelValidationError, User] = {
    if (uid < 1) return Left(ModelValidationError("UID must be greater than zero."))
    if (isEmpty(firstName)) return Left(ModelValidationError("First name must be present."))
    if (isEmpty(lastName)) return Left(ModelValidationError("Last name must be present."))

    Right(new User(uid, firstName, lastName))
  }

  private def isEmpty(s: String): Boolean = Option(s).getOrElse("").isEmpty
}

