package digital.porsche.ks.service

import digital.porsche.ks.model.Constants
import digital.porsche.ks.model.ProductStockState
import digital.porsche.ks.serializers.JsonSerializer
import digital.porsche.ks.serializers.StockStateDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

@Service
class StockManager {

    val stockStatesByShop = mutableMapOf<String, ProductStockState>()

    private val consumer: KafkaConsumer<String, ProductStockState>
    var running = true


    init {
        val config = Properties()
        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ConsumerConfig.GROUP_ID_CONFIG] = "stock-state-renderer-${Random.nextInt()}"
        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StockStateDeserializer::class.java
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        consumer = KafkaConsumer<String, ProductStockState>(config)

        thread {
            consumer.subscribe(listOf(Constants.TOPIC_STOCK_STATE))
            while (running){
                val records = consumer.poll(Duration.ofMillis(10))
                records.forEach {
                    val shopId = it.key()
                    val stockState = it.value()
                    stockStatesByShop[shopId] = stockState
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                running = false
            }
        )

        val producerConfig = Properties()
        producerConfig[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        producerConfig[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerConfig[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
    }

    fun getStockState(shopId: String): ProductStockState? = stockStatesByShop[shopId]

    fun getProductStockState(shopId: String, productId: String): Int =
        stockStatesByShop[shopId]?.productState?.get(productId) ?: 0


}