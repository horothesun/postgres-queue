package com.horothesun.postgresqueue

import cats.effect._
import cats.syntax.all._
import com.horothesun.postgresqueue.Models._
import dbclient._
import dbclient.Models._
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer
import scala.concurrent.duration.DurationInt
import skunk.Session
import skunk.syntax.all._

object TestDbClient {

  implicit val meter: Meter[IO] = Meter.noop
  implicit val tracer: Tracer[IO] = Tracer.noop

  val testDefaultVisibilityTimeout: QueueVisibilityTimeout = QueueVisibilityTimeout(5.seconds)

  val selfCleaningDbClient: Resource[IO, DbClient] =
    session.flatMap(s => Resource.make(DbClient.create(s, testDefaultVisibilityTimeout))(_ => truncateAllTables(s)))

  private def session: Resource[IO, Session[IO]] =
    Session
      .Builder[IO]
      .withHost("localhost")
      .withPort(5432)
      .withDatabase("test_db")
      .withUserAndPassword("postgres", "test_pwd")
      .single

  def populateQueues(db: DbClient, rs: List[QueueRow]): IO[Unit] =
    rs.traverseVoid(db.insertQueue)

  def populateMessages(db: DbClient, rs: List[MessageRow]): IO[Unit] =
    rs.traverseVoid(db.insertMessage)

  private def truncateAllTables(s: Session[IO]): IO[Unit] =
    s.execute(sql"TRUNCATE TABLE queues, messages".command).void

}
