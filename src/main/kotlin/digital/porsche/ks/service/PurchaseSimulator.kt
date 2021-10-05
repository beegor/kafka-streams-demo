package digital.porsche.ks.service

import digital.porsche.ks.model.*
import digital.porsche.ks.serializers.JsonSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.random.Random

@Service
class PurchaseSimulator (private val stockManager: StockManager){

    private lateinit var producer: KafkaProducer<String, Any>
    private val active = AtomicBoolean(true)

    private val restockTimes = mutableMapOf<String, Long>()

    @PostConstruct
    fun start() {
        val config = Properties()
        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        producer = KafkaProducer<String, Any>(config)

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                active.set(false)
            }
        )
        run()
    }


    private fun run(){
        thread {
            println("Purchase producing started!")

            while (active.get()) {
                val shop = getShops().random()
                val randomProducts: MutableMap<String, Int>  = mutableMapOf()
                (1..Random.nextInt(2, 7)).forEach {
                    val productId = ProductsOffered.get().random().id
                    val availableAmount = stockManager.getProductStockState(shop.id, productId)
                    if (availableAmount > 0) {
                        val amount = Random.nextInt(1, min(availableAmount + 1, 9))
                        randomProducts[productId] = amount
                    }
                    else
                        restockProduct(shop, productId)
                }

                val purchase = Purchase(shop.id, randomProducts)
                val producerRecord: ProducerRecord<String, Any> = ProducerRecord(Constants.TOPIC_PURCHASES, purchase.shopId, purchase)
                producer.send(producerRecord){ _, exception -> exception?.printStackTrace() }
                Thread.sleep(10)
            }
            println("Purchase producing stopped!")
        }
    }

    private fun restockProduct(shop: Shop, productId: String) {
        val key = "${shop.id}-$productId"
        val lastRestock = restockTimes[key]
        if ((lastRestock ?: 0) < System.currentTimeMillis() - 10000) {
            val stockEvent = StockEvent(StockEventType.ITEM_ADDED, productId, 1000)
            val producerRecord: ProducerRecord<String, Any> = ProducerRecord(Constants.TOPIC_STOCK_EVENTS, shop.id, stockEvent)
            producer.send(producerRecord)
            restockTimes[key] = System.currentTimeMillis()
        }
    }

}
