package partner

import authentication.{AuthenticationDao, Tokens}
import couple.{CoupleDao, CoupleRecord}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}
import user.{User, UserRegistration, UserService}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

class PartnerServiceImplTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {

  class UserServiceStub extends UserService {
    val userMap: mutable.Map[Int, Either[ErrorResponse, User]] = new mutable.HashMap[Int, Either[ErrorResponse, User]]

    override def registerUser(ur: UserRegistration): Future[Either[ErrorResponse, Tokens]] = ???

    override def getUser(uid: Int): Future[Either[ErrorResponse, User]] = Future.successful(
      userMap.getOrElse(uid, Left(NotFoundError("")))
    )
  }

  class CoupleDaoStub extends CoupleDao {
    override def storeCouple(uid: Int, pid: Int): Future[Int] = Future.successful(5)

    override def getCouple(cid: Int): Future[Option[CoupleRecord]] = ???

    override def connectUserToPartner(uid: Int, pid: Int, cid: Int): Future[Unit] = Future.successful()
  }

  class AuthenticationDaoStub extends AuthenticationDao {
    override def storeCredentials(uid: Int, password: String): Future[Tokens] = ???

    override def createTokens(uid: Int, pid: Option[Int], cid: Option[Int]): Future[Tokens] = Future.successful(stubTokens)
  }

  var partnerService: PartnerService = _
  var userServiceStub: UserServiceStub = _
  val stubTokens: Tokens = Tokens("testAccessToken", "testRefreshToken")

  override def beforeEach(): Unit = {
    userServiceStub = new UserServiceStub()
    partnerService = new PartnerServiceImpl(userServiceStub, new CoupleDaoStub(), new AuthenticationDaoStub())
  }

  "if the partnership is valid it" should "create tokens" in {
    userServiceStub.userMap.put(1, Right(User(1, None, None, "User", "Last")))
    userServiceStub.userMap.put(10, Right(User(10, None, None, "Partner", "Last")))

    partnerService.connectUsers(1, 10).map(errorOrTokens => {
      errorOrTokens shouldBe Right(stubTokens)
    })
  }

  "if the user is already connected it" should "return a client error" in {
    userServiceStub.userMap.put(1, Right(User(1, Option(2), Option(3), "First", "Last")))

    partnerService.connectUsers(1, 10).map(errorOrTokens => {
      errorOrTokens shouldBe Left(ClientError("User already has a partner."))
    })
  }

  "if the partner does not exist it" should "return a not found error" in {
    userServiceStub.userMap.put(1, Right(User(1, None, None, "First", "Last")))

    partnerService.connectUsers(1, 10).map(errorOrTokens => {
      errorOrTokens shouldBe Left(NotFoundError(""))
    })
  }

  "if the partner already has a partner it" should "return a client error" in {
    userServiceStub.userMap.put(1, Right(User(1, None, None, "User", "Last")))
    userServiceStub.userMap.put(10, Right(User(10, Option(2), Option(3), "Partner", "Last")))

    partnerService.connectUsers(1, 10).map(errorOrTokens => {
      errorOrTokens shouldBe Left(ClientError("Partner already has a partner."))
    })
  }

}
