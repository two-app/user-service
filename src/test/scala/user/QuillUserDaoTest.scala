package user

import java.util.Date

import db.FlywayHelper
import db.ctx._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class QuillUserDaoTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    val flyway = FlywayHelper.getFlyway
    flyway.clean()
    println("Cleaned database")
    flyway.migrate()
    println("Migrated DB")
  }

  "retrieving a newly created user" should "return the correct user" in {
    val q = quote {
      querySchema[UserRecord]("user").insert(
        lift(newUserRegistration(0))
      ).returningGenerated(_.uid)
    }

    db.ctx.run(q).flatMap(uid => new QuillUserDao().getUser(uid)).map(record => {
      record.isDefined shouldBe true
      record.get shouldEqual newUserRegistration(1, createdAt = record.get.createdAt)
    })
  }

  def newUserRegistration(uid: Int, createdAt: Date = new Date()): UserRecord = UserRecord(
    uid, None, None, "test@two.com", "First", "Last", acceptedTerms = true, ofAge = true, createdAt
  )
}
