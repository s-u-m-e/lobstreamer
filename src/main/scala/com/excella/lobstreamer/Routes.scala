package com.excella.lobstreamer

import javax.ws.rs.Path

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import com.excella.lobstreamer.modules.{ ActorModule, GraphModule, S3Module, crypto }
import io.swagger.annotations._

/**
 * Created by mmekuria on 12/3/17.
 */
@Api(value = "/files")
@Path("/files")
class Routes(modules: ActorModule with S3Module with GraphModule) extends Directives {

  implicit val materializer = modules.materializer

  @ApiOperation(value = "Upload a file", httpMethod = "POST", response = classOf[String])
  def upload = post {
    fileUpload("attachment") {
      case (metadata, byteSource) => {
        onSuccess(modules.encryptAndStore(byteSource, modules.s3Sink(metadata.fileName)).run()) { result =>
          complete(result.location.toString())
        }
      }
    }
  }

  @ApiOperation(value = "Download a file", httpMethod = "GET", response = classOf[ByteString])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName", required = true, dataType = "String", paramType = "path",
      value = "name of file that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def download = get {
    path(Segment) { fileName =>
      complete(HttpEntity(
        ContentTypes.`application/octet-stream`,
        modules.decryptAndServe(modules.s3Source(fileName))
      ))
    }
  }

  lazy val routes = logRequest("lobstreamer") {
    pathPrefix("files") {
      upload ~ download
    }

  }

}
