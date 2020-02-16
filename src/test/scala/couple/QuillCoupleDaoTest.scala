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
      .map(cid => {
        cid should be > 0; cid
      })
      .flatMap(cid => coupleDao.getCouple(cid))
      .map(coupleRecord => {
        coupleRecord.isDefined shouldBe true
        coupleRecord.get.uid should be > 0
        coupleRecord.get.pid should be > 0
      })
  }

  "connecting a user" should "update their PID" in {
    (for {
      uid <- userDao.storeUser(newUser()).map(maybeError => maybeError.right.get)
      pid <- userDao.storeUser(newUser()).map(maybeError => maybeError.right.get)
    } yield (uid, pid))
      .flatMap(ids => coupleDao.storeCouple(ids._1, ids._2).map((ids._1, ids._2, _)))
      .flatMap(ids => coupleDao.connectUserToPartner(ids._1, ids._2, ids._3).map(_ => ids))
      .flatMap(ids => userDao.getUser(ids._1).map((ids._1, ids._2, ids._3, _)))
      .map(data => {
        val record = data._4
        record.isDefined shouldBe true
        record.get.uid shouldBe data._1
        record.get.pid shouldBe Option(data._2)
        record.get.cid shouldBe Option(data._3)
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
