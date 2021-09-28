package digital.porsche.ks.service

import digital.porsche.ks.domain.ProductStockState
import digital.porsche.ks.domain.Purchase
import digital.porsche.ks.domain.StockEvent
import digital.porsche.ks.domain.StockEventType
import digital.porsche.ks.serializers.JsonSerializer
import digital.porsche.ks.serializers.StockStateDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

@Service
class StockManager {

    private val consumer: KafkaConsumer<String, ProductStockState>
    var running = true

    val stockStatesByShop = mutableMapOf<String, ProductStockState>()

    private val producer: KafkaProducer<String, StockEvent>

    init {
        val config = Properties()
        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ConsumerConfig.GROUP_ID_CONFIG] = "stock-state-renderer-${Random.nextInt()}"
        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StockStateDeserializer::class.java
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        consumer = KafkaConsumer<String, ProductStockState>(config)

        thread {
            consumer.subscribe(listOf("stock-state"))
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
        producer = KafkaProducer<String, StockEvent>(producerConfig)
    }

    fun getStockState(shopId: String): ProductStockState? = stockStatesByShop[shopId]

    fun getProductStockState(shopId: String, productId: String): Int =
        stockStatesByShop[shopId]?.productState?.get(productId) ?: 0

    fun restock(shopId: String, productId: String){
        producer.send (
            ProducerRecord("stock-events", shopId, StockEvent(StockEventType.ITEM_ADDED, productId, 1000))
        )
    }
}