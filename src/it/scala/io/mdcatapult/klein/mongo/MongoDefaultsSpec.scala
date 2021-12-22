package io.mdcatapult.klein.mongo

import com.typesafe.config.{Config, ConfigFactory}
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class MongoDefaultsSpec extends AnyFlatSpec with Matchers with ScalaFutures {

  implicit val config: Config = ConfigFactory.parseString(
    s"""
       |mongo {
       |  connection {
       |    host = "localhost"
       |    port = 27017
       |    username = "doclib"
       |    password = "doclib"
       |    database = "admin"
       |    srv = false
       |  }
       |}
    """.stripMargin)
  
  implicit val r: CodecRegistry = fromRegistries(
    fromProviders(classOf[TestDoc]),
    fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
    DEFAULT_CODEC_REGISTRY
  )

  val mongo: Mongo = new Mongo()

  "Mongo" should "have default secondary" in {
    val doc = mongo.readPreference.toDocument
    doc.getString("mode").asString().getValue should be("secondaryPreferred")
  }

  "Mongo" should "have default max staleness 2" in {
    val doc = mongo.readPreference.toDocument
    doc.getInt64("maxStalenessSeconds").asInt64().getValue should be(2)
  }
}
