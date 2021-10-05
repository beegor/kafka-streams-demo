package digital.porsche.ks.streams

import digital.porsche.ks.model.*
import digital.porsche.ks.serializers.createJSONSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class StockEventsProcessor {

    private val streamingApp:KafkaStreams

    init {
        val config = Properties()
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "stock-state-app"
        config[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 100
        streamingApp = KafkaStreams(buildTopology(), config)
        streamingApp.start()
    }

    private fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        val stockEventsStream: KStream<String, StockEvent> = builder.stream(
            Constants.TOPIC_STOCK_EVENTS,
            Consumed.with(Serdes.String(), createJSONSerde(StockEvent::class.java))
        )

        stockEventsStream
            .groupByKey()
            .aggregate(
                { ProductStockState() },

                { shopId, stockEvent, stockState ->
                    stockState.apply {
                        when (stockEvent.eventType) {
                            StockEventType.ITEM_ADDED -> addProducts(stockEvent.productId, stockEvent.amount)
                            StockEventType.ITEM_REMOVED -> removeProducts(stockEvent.productId, stockEvent.amount)
                        }
                    }
                },

                Materialized.with(Serdes.String(), createJSONSerde(ProductStockState::class.java))
            )
            .toStream()
            .to(Constants.TOPIC_STOCK_STATE)

        val topology = builder.build()
        println("StockEvents processor " + topology.describe().toString())
        return topology
    }

}