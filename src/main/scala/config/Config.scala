package config

import com.typesafe.config.ConfigFactory

object Config {
  def getProperty(k: String): String = ConfigFactory.load(getClass.getClassLoader).getString(k)
}
