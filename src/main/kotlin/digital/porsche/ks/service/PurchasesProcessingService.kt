package digital.porsche.ks.service

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
class PurchasesProcessingService {

    private val streamingApp:KafkaStreams

    init {
        val config = Properties()
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "purchase-processing-app"
        config[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 100
        streamingApp = KafkaStreams(buildTopology(), config)
        streamingApp.start()
    }

    private fun buildTopology(): Topology {
        val builder = StreamsBuilder()
        val purchasesStream: KStream<String, Purchase> = builder.stream(
            "purchases",
            Consumed.with(Serdes.String(), createJSONSerde(Purchase::class.java))
        )

        purchasesStream
            .flatMapValues { purchase ->
                purchase.products.entries.map { StockEvent(StockEventType.ITEM_REMOVED, it.key, it.value) }
            }
            .to(
                "stock-events",
                Produced.with( Serdes.String(), createJSONSerde(StockEvent::class.java))
            )


        val topology = builder.build()
        println(topology.describe().toString())
        return topology
    }

}