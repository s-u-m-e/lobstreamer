package com.excella.lobstreamer.modules

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
 * Created by mmekuria on 12/3/17.
 */

trait ActorModule {
  self: ConfigModule =>
  implicit val system = ActorSystem("lobstreamerSystem", config)
  implicit val materializer = ActorMaterializer()

}
