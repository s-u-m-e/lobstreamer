package com.excella.lobstreamer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.stream.ActorMaterializer
import com.excella.lobstreamer.modules._
import akka.http.scaladsl.server.Directives._
import com.github.swagger.akka.SwaggerSite

import scala.io.StdIn

/**
 * Created by mmekuria on 12/3/17.
 */
object Main extends App {

  val modules = new ActorModule with ConfigModule with S3Module with GraphModule with SwaggerSite

  implicit val materializer = modules.materializer
  implicit val system = modules.system
  implicit val ec = modules.system.dispatcher

  val bindingFuture = Http().bindAndHandle(new Routes(modules).routes ~ SwaggerService.routes ~ modules.swaggerSiteRoute, "localhost", 9000)

  println(s"Server online at http://localhost:9000/")

  StdIn.readLine()
  bindingFuture flatMap (_.unbind) onComplete (_ => modules.system.terminate)
}
