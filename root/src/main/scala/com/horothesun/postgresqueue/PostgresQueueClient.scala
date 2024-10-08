package com.horothesun.postgresqueue

import Models._
import cats.effect.IO

trait PostgresQueueClient {
  def createQueue(queueName: QueueName): IO[Unit]
  def createQueue(queueName: QueueName, queueVisibilityTimeout: QueueVisibilityTimeout): IO[Unit]
  def getQueueVisibilityTimeout(queueName: QueueName): IO[Option[QueueVisibilityTimeout]]
  def updateQueueVisibilityTimeout(queueName: QueueName, queueVisibilityTimeout: QueueVisibilityTimeout): IO[Unit]
  def deleteQueue(queueName: QueueName): IO[Unit]
  def publishMessage(messageBody: MessageBody): IO[Unit]
}

object PostgresQueueClient {}
