package health

import cats.implicits._
import cats.data.EitherT
import response.ErrorResponse.InternalError
import com.typesafe.scalalogging.Logger
import cats.Functor

trait HealthService[F[_]] {
  def getHealth(): EitherT[F, InternalError, Int]
}

class HealthServiceImpl[F[_]: Functor](
    healthDao: HealthDao[F]
) extends HealthService[F] {

  val logger: Logger = Logger(classOf[HealthServiceImpl[F]])

  override def getHealth(): EitherT[F, InternalError, Int] = {
    healthDao.performSimpleStatement()
      .leftMap(exception => {
        logger.error("Failed to interact with database.", exception)
        InternalError(
          s"Failed to interact with database. Exception message: ${exception.getMessage()}"
        )
      })
  }

}
