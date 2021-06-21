package io.mdcatapult.klein.mongo

import com.mongodb.{MongoClientSettings, ReadPreference}
import com.typesafe.config.Config
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala._
import org.mongodb.scala.connection.{ClusterSettings, NettyStreamFactoryFactory}

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
    .readPreference(ReadPreference.valueOf(Try(config.getString("mongo.connection.readPreference")).getOrElse("secondaryPreferred")))
    .codecRegistry(codecs)

  def applySslSettings(builder: MongoClientSettings.Builder): MongoClientSettings.Builder = {
    val enableFlag = "mongo.connection.tls.enabled"
    val isEnabled = config.hasPath(enableFlag) && config.getBoolean(enableFlag)

    if (isEnabled)
      builder.streamFactoryFactory(NettyStreamFactoryFactory())
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
}
