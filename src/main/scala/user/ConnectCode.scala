package user

import com.typesafe.config.ConfigFactory
import org.hashids.Hashids

object ConnectCode {
  def toId(connectCode: String): Option[Int] = {
    val idParts = new Hashids(ConfigFactory.load().getString("hashids.salt"), 6).decode(connectCode)
    if (idParts.isEmpty) None else Option(idParts(0).toInt)
  }
}
