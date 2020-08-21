package io.mdcatapult.klein.mongo

import com.mongodb.MongoClientSettings
import com.typesafe.config.Config
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala._
import org.mongodb.scala.connection.NettyStreamFactoryFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class Mongo()(implicit config: Config, codecs: CodecRegistry = MongoClient.DEFAULT_CODEC_REGISTRY, ec: ExecutionContext) {
  val credential: MongoCredential = MongoCredential.createCredential(
    config.getString("mongo.connection.username"),
    config.getString("mongo.connection.database"),
    config.getString("mongo.connection.password").toCharArray
  )

  val hosts: List[String] = {
    val configured = config.getStringList("mongo.connection.hosts").asScala.toList
    if (configured.nonEmpty)
      configured
    else
      List("localhost")
  }

  val builder: MongoClientSettings.Builder = MongoClientSettings.builder()
    .credential(credential)
    .applyToClusterSettings(b => b.hosts(
      (
        for (host <- hosts)
          yield new ServerAddress(host)
        ).asJava
    ))
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
  val database: MongoDatabase = mongoClient.getDatabase(config.getString("mongo.database"))
  val collection: MongoCollection[Document] = database.getCollection(config.getString("mongo.collection"))

  def getCollection(collectionName: Option[String] = None): MongoCollection[Document] = collectionName match {
    case Some(name: String) => database.getCollection(name)
    case None => collection
  }

  def checkHealth(): Future[Boolean] = {
    mongoClient.listDatabaseNames().toFuture().map(_ => true).recover{case _ => false}
  }
}
