package com.twitter.unittests.integration.async_transformer

import com.twitter.finatra.kafka.serde.{ScalaSerdes, UnKeyedSerde}
import com.twitter.finatra.kafkastreams.KafkaStreamsTwitterServer
import com.twitter.finatra.kafkastreams.processors.FlushingAwareServer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{Consumed, Produced}

class WordLookupAsyncServer extends KafkaStreamsTwitterServer with FlushingAwareServer {

  override val name = "wordcount"

  override protected def configureKafkaStreams(builder: StreamsBuilder): Unit = {
    val supplier = () => new WordLookupAsyncTransformer(streamsStatsReceiver, commitInterval())

    builder.asScala
      .stream("TextLinesTopic")(Consumed.`with`(UnKeyedSerde, Serdes.String))
      .flatMapValues(_.split(' '))
      .transform(supplier)
      .to("WordToWordLength")(Produced.`with`(Serdes.String, ScalaSerdes.Long))
  }
}
