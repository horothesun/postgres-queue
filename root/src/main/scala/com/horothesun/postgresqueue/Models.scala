package com.horothesun.postgresqueue

import scala.concurrent.duration._
import skunk.Codec
import skunk.codec.all._

object Models {

  case class QueueName(value: String)
  object QueueName {
    implicit val codec: Codec[QueueName] = varchar(50).to[QueueName]
  }
  case class QueueVisibilityTimeout(value: Duration)
  object QueueVisibilityTimeout {
    implicit val codec: Codec[QueueVisibilityTimeout] =
      int8.imap[Duration](_.seconds)(_.toSeconds).to[QueueVisibilityTimeout]
  }

  case class MessageId(value: Int)
  object MessageId {
    implicit val codec: Codec[MessageId] = int4.to[MessageId]
  }
  case class MessageBody(value: String)
  object MessageBody {
    implicit val codec: Codec[MessageBody] = varchar(500).to[MessageBody]
  }

}
