package com.excella.lobstreamer.modules

import java.nio.file.Paths
import javax.crypto.Cipher

import akka.NotUsed
import akka.stream.alpakka.s3.scaladsl.MultipartUploadResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.{Broadcast, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.stage.GraphStage
import akka.stream.{ClosedShape, FlowShape, SourceShape, UniformFanOutShape}
import akka.util.ByteString
import com.excella.lobstreamer.modules.crypto._

import scala.concurrent.Future

/**
 * Created by mmekuria on 12/11/17.
 */
trait GraphModule {
  import GraphDSL.Implicits._
  import crypto._

  val encryptionFlow: GraphStage[FlowShape[ByteString, ByteString]] = new AesStage(encryptionCipher)
  val decryptionFlow: GraphStage[FlowShape[ByteString, ByteString]] = new AesStage(decryptionCipher)


  def encryptAndStore(fromRequest: Source[ByteString, Any], sink: Sink[ByteString, Future[MultipartUploadResult]]) =
    RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit b => s =>
      val splitter = b.add(Broadcast[ByteString](2))

      fromRequest ~> encryptionFlow ~> splitter ~> s
      splitter ~> FileIO.toPath(Paths.get("random.pdf"))
      ClosedShape
    });

  def decryptAndServe(source: Source[ByteString, NotUsed]) = Source.fromGraph(GraphDSL.create() {
    implicit b =>
      val s3source = b.add(source)
      val splitter = b.add(Broadcast[ByteString](2))


      val dfl = b.add(decryptionFlow)
      s3source ~> dfl ~> splitter
                         splitter ~> FileIO.toPath(Paths.get("downloaded.pdf"))
      SourceShape(splitter.out(1))
  });

}
