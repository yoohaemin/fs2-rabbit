/*
 * Copyright 2017-2019 Fs2 Rabbit
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

package com.github.gvolpe.fs2rabbit.json

import cats.effect.Sync
import com.github.gvolpe.fs2rabbit.model.{AmqpEnvelope, DeliveryTag}
import com.github.gvolpe.fs2rabbit.effects.Log
import fs2.{Pipe, Stream}
import io.circe.parser.decode
import io.circe.{Decoder, Error}

/**
  * Stream-based Json Decoder that exposes only one method as a streaming transformation
  * using `fs2.Pipe` and depends on the Circe library.
  * */
class Fs2JsonDecoder[F[_]: Log: Sync] {

  /**
    * It tries to decode an `AmqpEnvelope.payload` into a case class determined by the parameter [A].
    *
    * For example:
    *
    * {{{
    * import fs2._
    *
    * val json = """ { "two": "the two" } """
    * val envelope = AmqpEnvelope(1, json, AmqpProperties.empty)
    *
    * val p = Stream(envelope).covary[IO] through jsonDecode[IO, Person]
    *
    * p.run.unsafeRunSync
    * }}}
    *
    * The result will be a tuple (`Either` of `Error` and `A`, `DeliveryTag`)
    * */
  def jsonDecode[A: Decoder]: Pipe[F, AmqpEnvelope[String], (Either[Error, A], DeliveryTag)] = _.flatMap { amqpMsg =>
    Stream
      .eval(Sync[F].delay(decode[A](amqpMsg.payload)))
      .evalTap(p => Log[F].info(s"Parsed: $p"))
      .map(p => (p, amqpMsg.deliveryTag))
  }

}
