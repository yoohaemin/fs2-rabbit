/*
 * Copyright 2017 Fs2 Rabbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gvolpe.fs2rabbit.examples

import cats.effect.IO
import com.github.gvolpe.fs2rabbit.StreamLoop
import com.github.gvolpe.fs2rabbit.config.Fs2RabbitConfig
import com.github.gvolpe.fs2rabbit.interpreter.Fs2Rabbit
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.ExecutionContext

object MonixAutoAckConsumer extends IOApp {

  implicit val appS: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val config: Fs2RabbitConfig = Fs2RabbitConfig(virtualHost = "/",
                                                        host = "127.0.0.1",
                                                        username = Some("guest"),
                                                        password = Some("guest"),
                                                        port = 5672,
                                                        ssl = false,
                                                        connectionTimeout = 3,
                                                        requeueOnNack = false)

  override def start(args: List[String]): IO[Unit] =
    Fs2Rabbit[Task](config).flatMap { implicit interpreter =>
      StreamLoop.run(() => new AutoAckConsumerDemo[Task].program)
    }.toIO
}