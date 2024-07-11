/*
 * Copyright 2024 Medicines Discovery Catapult
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doclib.mongo

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

  "Mongo" should "have default max staleness 90" in {
    val doc = mongo.readPreference.toDocument
    doc.getInt64("maxStalenessSeconds").asInt64().getValue should be(90)
  }

  "Mongo" should "use secondary and max staleness 90 if readPreference config specified wrong " in {
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
         |    readPreference = "primaryPlease"
         |    maxStaleness = 5
         |  }
         |}
    """.stripMargin)
    val mongo: Mongo = new Mongo()
    val readPreference = mongo.readPreference.toDocument
    readPreference.getString("mode").asString().getValue should be("secondaryPreferred")
    // Config says 5 but values less than 90 are changed to 90
    readPreference.getInt64("maxStalenessSeconds").asInt64().getValue should be(90)
  }
}
