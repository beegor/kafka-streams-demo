package digital.porsche.ks.streams

import digital.porsche.ks.model.*
import digital.porsche.ks.serializers.createJSONSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueIterator
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*


@Service
class ProfitCalculator {

    private val streamingApp:KafkaStreams

    init {
        val config = Properties()
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "profit-calculator-app"
        config[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 100
        streamingApp = KafkaStreams(buildTopology(), config)
        streamingApp.start()
    }

    private fun buildTopology(): Topology {

        val builder = StreamsBuilder()

        val purchasesStream: KStream<String, Purchase> = builder.stream(
            Constants.TOPIC_PURCHASES,
            Consumed.with(Serdes.String(), createJSONSerde(Purchase::class.java))
        )

        val productPurchaseStream: KStream<String, ProductPurchase> =
            purchasesStream
                .flatMapValues { purchase ->
                    purchase.products.entries.map { entry -> ProductPurchase(entry.key, entry.value) }
                }

        val summedProductPurchases: KStream<String, ProductPurchase> = productPurchaseStream
            .groupBy (
                { shopId, productPurchase -> productPurchase.productId },
                Grouped.with(Serdes.String(), createJSONSerde(ProductPurchase::class.java))
            )
            .reduce { v, v2 ->
                v + v2
            }
            .toStream()

        val productsKTable: GlobalKTable<String, Product> = builder.globalTable(
            Constants.TOPIC_PRODUCTS,
            Consumed.with(Serdes.String(), createJSONSerde(Product::class.java))
        )

        summedProductPurchases
            .join (
                productsKTable,
                { productId, productPurchase -> productId },
                { productPurchase, product ->
                    val profit = (product.sellingPrice.subtract(product.buyingPrice)).multiply( BigDecimal(productPurchase.amount))
                    ProductReport(product.id, product.name, productPurchase.amount, profit)
                }
            )
            .toTable(
                Materialized
                    .`as`<String, ProductReport, KeyValueStore<Bytes, ByteArray>>("profitsStore")
                    .withKeySerde(Serdes.String()).withValueSerde(createJSONSerde(ProductReport::class.java))
            )

        val topology = builder.build()
        println("Profit calculator " + topology.describe().toString())
        return topology
    }



    fun getProfits() : Map<String, ProductReport> {

        val keyValueStore: ReadOnlyKeyValueStore<String, ProductReport> = streamingApp.store(
            StoreQueryParameters.fromNameAndType(
                "profitsStore",
                QueryableStoreTypes.keyValueStore()
            )
        )
        val profitsDataIterator: KeyValueIterator<String, ProductReport>  = keyValueStore.all()

        val results = mutableMapOf<String, ProductReport>()

        while (profitsDataIterator.hasNext()) {
            val next: KeyValue<String, ProductReport>  = profitsDataIterator.next()
            results[next.key] = next.value
        }
        profitsDataIterator.close()
        return results
    }
}

