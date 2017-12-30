package com.excella.lobstreamer.modules

import java.io.File

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Created by mmekuria on 12/3/17.
 */

trait ConfigModule {
  lazy val internalConfig = ConfigFactory.load()

  def config = scala.sys.props.get("application.config") match {
    case Some(filename) => ConfigFactory.parseFile(new File(filename)).withFallback(internalConfig)
    case None => internalConfig
  }
}
