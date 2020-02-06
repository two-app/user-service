package user

import db.DatabaseError.DuplicateEntry
import db.FlywayHelper
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class QuillUserDaoTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  val userDao: UserDao = new QuillUserDao

  override def beforeEach(): Unit = {
    val flyway = FlywayHelper.getFlyway
    flyway.clean()
    flyway.migrate()
  }

  "storing a user" should "return the generated UID" in {
    userDao.storeUser(newUserRegistration("user1@two.com"))
      .map(errorOrUid => {
        errorOrUid.isRight shouldBe true
        errorOrUid.right.get shouldBe 1
      })
      .flatMap(_ => userDao.storeUser(newUserRegistration("user2@two.com"))
        .map(errorOrUid => {
          errorOrUid.isRight shouldBe true
          errorOrUid.right.get shouldBe 2
        }))
  }

  "storing two users with the same email" should "return a DatabaseError" in {
    val user = newUserRegistration("user@two.com")
    userDao.storeUser(user).flatMap(_ => userDao.storeUser(user)).map(errorOrUid => {
      errorOrUid.isLeft shouldBe true
      errorOrUid.left.get shouldBe DuplicateEntry()
    })
  }

  "retrieving a newly created user" should "return the correct user" in {
    val email = "test@two.com"
    userDao.storeUser(newUserRegistration(email)).map(eitherDatabaseErrorOrUid => {
      eitherDatabaseErrorOrUid.isRight shouldBe true
      eitherDatabaseErrorOrUid.right.get
    }).flatMap(userDao.getUser).map(record => {
      record.isDefined shouldBe true
      record.get.uid should be > 0
      record.get.email shouldBe email
    })
  }

  def newUserRegistration(email: String): UserRegistration = UserRegistration(
    firstName = "First", lastName = "Last", email = email, acceptedTerms = true, ofAge = true, password = "SaFePassW0rD"
  )
}
