package digital.porsche.ks.streams

import digital.porsche.ks.model.*
import digital.porsche.ks.serializers.createJSONSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.util.*


@Service
class ProductProfitHistoryCalculator {

    private val streamingApp: KafkaStreams

    init {
        val config = Properties()
        config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "profit-history-calculator-app"
        config[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 1000
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


        val summedProductPurchases: KStream<Windowed<String>, ProductPurchase> = productPurchaseStream
            .groupBy (
                { shopId, productPurchase -> productPurchase.productId },
                Grouped.with(Serdes.String(), createJSONSerde(ProductPurchase::class.java))
            )
            .windowedBy(
                TimeWindows
                    .of(Duration.ofSeconds(5))
                    .grace(Duration.ofSeconds(5))
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
                { key, productPurchase -> key.key() },
                { productPurchase, product ->
                    val profit = (product.sellingPrice.subtract(product.buyingPrice)).multiply( BigDecimal(productPurchase.amount))
                    ProductReport(product.id, product.name, productPurchase.amount,profit)
                }
            )
            .map { key, pr ->
                val primaryKey = "${key.key()}-${key.window().start()}-${key.window().end()}"
                KeyValue(
                    primaryKey ,
                    ProductProfitHistory(
                        primaryKey,
                        pr.productId,
                        pr.productName,
                        pr.amountSold,
                        pr.profit,
                        key.window().start(),
                        key.window().end())
                )
            }
            .to(
                Constants.TOPIC_PRODUCT_PROFIT_REPORT,
                Produced.with(Serdes.String(), createJSONSerde(ProductProfitHistory::class.java))
            )


        val topology = builder.build()
        println(topology.describe().toString())
        return topology
    }

}
