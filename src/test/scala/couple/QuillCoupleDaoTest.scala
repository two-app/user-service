package couple

import db.FlywayHelper
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import user.{QuillUserDao, UserDao, UserRegistration}

import scala.util.Random

class QuillCoupleDaoTest extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  val coupleDao: CoupleDao = new QuillCoupleDao
  val userDao: UserDao = new QuillUserDao

  override def beforeEach(): Unit = {
    val flyway = FlywayHelper.getFlyway
    flyway.clean()
    flyway.migrate()
  }

  "storing a couple" should "return the generated CID" in {
    (for {
      uid <- userDao.storeUser(newUser()).map(maybeError => maybeError.right.get)
      pid <- userDao.storeUser(newUser()).map(maybeError => maybeError.right.get)
    } yield (uid, pid))
      .flatMap(ids => coupleDao.storeCouple(ids._1, ids._2))
      .map(cid => {cid should be > 0; cid})
      .flatMap(cid => coupleDao.getCouple(cid))
      .map(coupleRecord => {
        coupleRecord.isDefined shouldBe true
        coupleRecord.get.uid should be > 0
        coupleRecord.get.pid should be > 0
      })
  }

  "retrieving a couple that does not exist" should "return None" in {
    coupleDao.getCouple(10).map(maybeCouple => {
      maybeCouple.isEmpty shouldBe true
    })
  }

  def newUser(): UserRegistration = UserRegistration.from(
    "First", "Last", randomEmail(), "TestPassword", acceptedTerms = true, ofAge = true
  ).right.get

  def randomEmail(): String = "quillCoupleTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"

}
