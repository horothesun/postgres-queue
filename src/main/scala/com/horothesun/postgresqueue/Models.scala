package com.horothesun.postgresqueue

import skunk.codec.all._
import skunk.Codec

import scala.concurrent.duration._

object Models {

  case class QueueName(value: String)
  object QueueName {
    implicit val codec: Codec[QueueName] = varchar(50).gimap[QueueName]
  }
  case class QueueVisibilityTimeout(value: Duration)
  object QueueVisibilityTimeout {
    implicit val codec: Codec[QueueVisibilityTimeout] =
      int8.imap[Duration](_.seconds)(_.toSeconds).gimap[QueueVisibilityTimeout]
  }

  case class MessageId(value: Int)
  object MessageId {
    implicit val codec: Codec[MessageId] = int4.gimap[MessageId]
  }
  case class MessageBody(value: String)
  object MessageBody {
    implicit val codec: Codec[MessageBody] = varchar(500).gimap[MessageBody]
  }

}
