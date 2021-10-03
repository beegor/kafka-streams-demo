package digital.porsche.ks.service

import digital.porsche.ks.model.Constants
import digital.porsche.ks.model.ProductProfitHistory
import digital.porsche.ks.model.ProductStockState
import digital.porsche.ks.model.StockEvent
import digital.porsche.ks.repositories.ProductProfitHistoryRepository
import digital.porsche.ks.serializers.JsonSerializer
import digital.porsche.ks.serializers.ProductProfitHistoryDeserializer
import digital.porsche.ks.serializers.StockStateDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random

@Service
class ProductProfitHistoryManager(
    val productProfitHistoryRepository: ProductProfitHistoryRepository
) {

    private val consumer: KafkaConsumer<String, ProductProfitHistory>
    private var running = true

    init {
        val config = Properties()
        config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ConsumerConfig.GROUP_ID_CONFIG] = "product-profit-history-exporter"
        config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ProductProfitHistoryDeserializer::class.java
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        consumer = KafkaConsumer<String, ProductProfitHistory>(config)

        thread {
            consumer.subscribe(listOf(Constants.TOPIC_PRODUCT_PROFIT_REPORT))
            while (running){
                val records = consumer.poll(Duration.ofMillis(10))
                productProfitHistoryRepository.saveAll(records.map { it.value() })
            }
        }

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                running = false
            }
        )

    }

    fun getProfitHistory( timeFrom: Long, timeTo:Long ): List<ProductProfitHistory> {
        return productProfitHistoryRepository.findByTimeFromGreaterThanEqualAndTimeToLessThanEqual(timeFrom, timeTo)
    }



}