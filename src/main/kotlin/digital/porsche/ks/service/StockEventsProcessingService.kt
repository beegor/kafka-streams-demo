package digital.porsche.ks.service

import digital.porsche.ks.domain.ProductStockState
import digital.porsche.ks.domain.Purchase
import digital.porsche.ks.domain.StockEvent
import digital.porsche.ks.domain.StockEventType
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
class StockEventsProcessingService {

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
        val purchasesStream: KStream<String, StockEvent> = builder.stream(
            "stock-events",
            Consumed.with(Serdes.String(), createJSONSerde(StockEvent::class.java))
        )

        purchasesStream
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
            .to(
                "stock-state"
            )

        val topology = builder.build()
        println(topology.describe().toString())
        return topology
    }

}