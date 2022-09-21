package com.horothesun.postgresqueue.DbClient

import com.horothesun.postgresqueue.Models._
import skunk.Encoder
import skunk.codec.all._

import java.time.LocalDateTime

object Models {

  case class QueueRow(
    name: Queue.Name,
    visibilityTimeout: Queue.VisibilityTimeout
  )
  object QueueRow {
    implicit val skunkEncoder: Encoder[QueueRow] =
      (
        Queue.Name.skunkEncoder ~
          Queue.VisibilityTimeout.skunkEncoder
      ).values.contramap[QueueRow](q => (q.name, q.visibilityTimeout))
  }

  case class MessageRow(
    id: Message.Id,
    queueName: Queue.Name,
    body: Message.Body,
    enqueuedAt: LocalDateTime,
    lastReadAt: LocalDateTime,
    dequeuedAt: LocalDateTime
  )
  object MessageRow {
    implicit val skunkEncoder: Encoder[MessageRow] =
      (
        Message.Id.skunkEncoder ~
          Queue.Name.skunkEncoder ~
          Message.Body.skunkEncoder
        // TODO: continue... 🔥🔥🔥
      ).values.contramap[MessageRow](m => ((m.id, m.queueName), m.body))
  }

}
