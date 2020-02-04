package user

import java.util.Date

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import db.ctx._

class QuillUserDaoTest extends AnyFlatSpec with Matchers {

  import scala.concurrent.ExecutionContext.Implicits.{ global => ec }

  "retrieving a newly created user" should "return the correct user" in {
    val q = quote {
      querySchema[UserRecord]("user").insert(
        lift(UserRecord(0, None, None, "test@two.com", "First", "Last", acceptedTerms = true, ofAge = true, new Date()))
      ).returningGenerated(_.uid)
    }

    db.ctx.run(q).flatMap(uid => new QuillUserDao().getUser(uid)).map(record => {
      record.isDefined shouldBe true
    })
  }
}
