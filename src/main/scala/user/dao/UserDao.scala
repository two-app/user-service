package user.dao

import java.util.Date

import db.ctx._
import user.model.ObjectValidationError.InvalidUserError

case class User(
                 uid: Int,
                 pid: Option[Int],
                 cid: Option[Int],
                 email: String,
                 firstName: String,
                 lastName: String,
                 acceptedTerms: Boolean,
                 ofAge: Boolean,
                 createdAt: Date
               )

class UserDao {
  def getUser: Option[Either[InvalidUserError, user.model.User]] = run(quote {
    query[User].filter(u => u.email == "1999Gerry@gmail.com")
  }).headOption.map(row => user.model.User.from(row.uid, row.firstName, row.lastName))
}

object UserDao {

}
