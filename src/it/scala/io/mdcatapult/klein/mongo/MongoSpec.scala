package io.mdcatapult.klein.mongo

import java.time.LocalDateTime.now
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID.randomUUID

import com.typesafe.config.{Config, ConfigFactory}
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters.{equal => Mequal}
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class MongoSpec extends AnyFlatSpec with Matchers with ScalaFutures {

  "Mongo" should "write test document when configured" in {
    implicit val c: Config = ConfigFactory.load()
    implicit val r: CodecRegistry = fromRegistries(
      fromProviders(classOf[TestDoc]),
      fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
      DEFAULT_CODEC_REGISTRY
    )

    val mongo: Mongo = new Mongo()

    implicit val collection: MongoCollection[TestDoc] =
      mongo.database.getCollection(
        c.getString("mongo.collection")
      )

    val doc = TestDoc(randomUUID(), now(UTC))

    val written = collection.insertOne(doc).toFutureOption()
    val read = written.flatMap(_ => collection.find(Mequal("_id", doc._id)).toFuture())

    read.recover {
      case e: Exception =>
        e.printStackTrace()
        fail(e.toString)
    }

    whenReady(read) { docs =>
      val storedDoc = docs.headOption.value

      storedDoc._id should be(doc._id)
      storedDoc.created.truncatedTo(MILLIS) should be (doc.created.truncatedTo(MILLIS))
    }
  }
}
