package db

import java.time.Instant

import doobie.util.meta.Meta

object DateTimeModule {

  implicit val instantMeta: Meta[Instant] = Meta[Long].timap(fromInstant)(toInstant)

  def fromInstant(epochMillis: Long): Instant = Instant.ofEpochMilli(epochMillis)

  def toInstant(instant: Instant): Long = instant.toEpochMilli

}
