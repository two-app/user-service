package health

trait HealthService[F[_]] {}

class HealthServiceImpl[F[_]](healthDao: HealthDao[F])
    extends HealthService[F] {}
