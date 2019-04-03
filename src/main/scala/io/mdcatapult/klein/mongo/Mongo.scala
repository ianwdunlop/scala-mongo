package io.mdcatapult.klein.mongo

import java.util

import com.mongodb.MongoClientSettings
import com.typesafe.config.Config
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala._

import scala.collection.JavaConverters._

class Mongo()(implicit config: Config, codecs: CodecRegistry = MongoClient.DEFAULT_CODEC_REGISTRY) {
  val credential: MongoCredential = MongoCredential.createCredential(
    config.getString("mongo.connection.username"),
    config.getString("mongo.connection.database"),
    config.getString("mongo.connection.password").toCharArray
  )

  val settings: MongoClientSettings = MongoClientSettings.builder()
    .credential(credential)
    .applyToClusterSettings(b => b.hosts(
      (
        for (host: String ← config.getStringList("mongo.connection.hosts").asScala.toList)
          yield new ServerAddress(host)
        ).asJava
    ))
    .codecRegistry(codecs)
    .build()


  val mongoClient: MongoClient = MongoClient(settings)
  val database: MongoDatabase = mongoClient.getDatabase(config.getString("mongo.database"))
  val collection: MongoCollection[Document] = database.getCollection(config.getString("mongo.collection"))

  def getCollection(collectionName: Option[String] = None): MongoCollection[Document] = collectionName match {
    case Some(name: String) ⇒ database.getCollection(name)
    case None ⇒ collection
  }
}
