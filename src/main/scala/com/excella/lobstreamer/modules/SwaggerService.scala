package com.excella.lobstreamer.modules

import com.excella.lobstreamer.Routes
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.annotations.{ BasicAuthDefinition, ExternalDocs }

/**
 * Created by mmekuria on 12/25/17.
 */
object SwaggerService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[Routes])
  override val host = "localhost:9000"
  override val info = Info(version = "1.0")
  //override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
  //override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")

}
