package com.horothesun.postgresqueue

import cats.effect._
//import cats.implicits._
import natchez.Trace.Implicits.noop
//import skunk.implicits._
import skunk.Session

object TestDbClient {

  val session: Resource[IO, Session[IO]] =
    Session
      .single[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "test_db",
        password = Some("test_pwd")
      )
//      .flatTap(withMessagesTable)
//      .flatTap(withQueuesTable)
      .evalTap(populateTable)

//  private def withMessagesTable(s: Session[IO]): Resource[IO, Unit] =
//    Resource.make(
//      s.execute(sql"""
//         CREATE TABLE messages (
//           message_id varchar,
//           queue_id varchar,
//           body varchar,
//           enqueued_at timestamp,
//           last_read_at timestamp,
//           dequeued_at timestamp
//         )
//         """.command)
//        .void
//    )(_ => s.execute(sql"DROP TABLE messages".command).void)
//
//  private def withQueuesTable(s: Session[IO]): Resource[IO, Unit] =
//    Resource.make(
//      s.execute(sql"""
//        CREATE TABLE queues (
//          queue_id varchar,
//          visibility_timeout_sec int4
//        )
//        """.command)
//        .void
//    )(_ => s.execute(sql"DROP TABLE queues".command).void)

  def populateTable(s: Session[IO]): IO[Unit] = IO(42).void

}
