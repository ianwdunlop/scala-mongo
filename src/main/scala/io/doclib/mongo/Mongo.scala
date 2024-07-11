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

import com.mongodb.{MongoClientSettings, ReadPreference}
import com.typesafe.config.Config
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala._
import org.mongodb.scala.connection.{ClusterSettings, TransportSettings}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.util.Try

class Mongo()(implicit config: Config, codecs: CodecRegistry = MongoClient.DEFAULT_CODEC_REGISTRY, ec: ExecutionContext) {
  val credential: MongoCredential = MongoCredential.createCredential(
    config.getString("mongo.connection.username"),
    config.getString("mongo.connection.database"),
    config.getString("mongo.connection.password").toCharArray
  )

  val hosts: List[String] = config.getString("mongo.connection.host").split(",").toList

  // By default it uses secondary with replication lag max of 90 seconds (the minimum that mongodb allows)
  val readPreference: ReadPreference = Try(config.getString("mongo.connection.readPreference")).getOrElse("secondaryPreferred") match {
    case "secondaryPreferred" => ReadPreference.secondaryPreferred(getMaxStaleness(), TimeUnit.SECONDS)
    case "primaryPreferred" => ReadPreference.primaryPreferred()
    case _ => ReadPreference.secondaryPreferred(getMaxStaleness(), TimeUnit.SECONDS)
  }
  private val builder = MongoClientSettings.builder()
    .credential(credential)
    .applyToClusterSettings(
      (builder: ClusterSettings.Builder) => {
        if (config.getBoolean("mongo.connection.srv"))
          builder.srvHost(hosts.head)
        else
          builder.hosts((for (host <- hosts)
            yield new ServerAddress(host, config.getInt("mongo.connection.port"))).asJava)
      })
    .readPreference(readPreference)
    .codecRegistry(codecs)

  def applySslSettings(builder: MongoClientSettings.Builder): MongoClientSettings.Builder = {
    val enableFlag = "mongo.connection.tls.enabled"
    val isEnabled = config.hasPath(enableFlag) && config.getBoolean(enableFlag)

    if (isEnabled)
      builder.transportSettings(TransportSettings.nettyBuilder().build())
        .applyToSslSettings(b => b.enabled(true))
    else
      builder
  }

  val settings: MongoClientSettings = applySslSettings(builder).build()
  val mongoClient: MongoClient = MongoClient(settings)

  def getDatabase(databaseName: String): MongoDatabase = {
    mongoClient.getDatabase(databaseName)
  }

  def getCollection[T:ClassTag](databaseName: String, collectionName: String): MongoCollection[T] = {
    mongoClient.getDatabase(databaseName).getCollection[T](collectionName)
  }

  def checkHealth(): Future[Boolean] = {
    mongoClient.listDatabaseNames().toFuture().map(_ => true).recover{case _ => false}
  }

  /**
   * Get the max staleness config setting (in seconds) for secondary replication lag. It
   * cannot be less than 90 seconds
   *
   * @return
   */
  def getMaxStaleness(): Long = {
    Try(config.getLong("mongo.connection.maxStaleness")).getOrElse(90L) match {
      case maxStaleness if maxStaleness >= 90 => maxStaleness
      case maxStaleness if maxStaleness < 90 => 90
      case _ => 90
    }
  }
}
