package repositories

import com.typesafe.config.ConfigFactory
import org.scalatest._
import play.api.Configuration
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object MongoSuite {

  private lazy val config = Configuration(
    ConfigFactory.load(
      System.getProperty(
        "config.resource"
      )))

  private lazy val parsedUri: Future[MongoConnection.ParsedURI] = Future.fromTry {
    MongoConnection.parseURI(config.get[String]("mongodb.uri"))
  }

  lazy val connection: Future[Try[MongoConnection]] = parsedUri.map { MongoDriver().connection(_, strictUri = false) }
}

trait MongoSuite {
  self: TestSuite =>

  def database: Future[DefaultDB] =
    for {
      uri              <- MongoSuite.parsedUri
      connectionFuture <- MongoSuite.connection
      connection       <- Future.fromTry(connectionFuture)
      database         <- connection.database(uri.db.get)
    } yield database

}
