package digital.porsche.ks.service

import digital.porsche.ks.domain.ProductsOffered
import digital.porsche.ks.domain.Purchase
import digital.porsche.ks.domain.getShops
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
class DemoProducer (private val stockManager: StockManager){

    private val producer: KafkaProducer<String, Purchase>
    private val active = AtomicBoolean(true)

    init {
        val config = Properties()
        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        producer = KafkaProducer<String, Purchase>(config)

        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                active.set(false)
            }
        )
        run()
    }



    @PostConstruct
    private fun run(){
        thread {
            println("Purchase producing started!")
            var successCount = 0
            var failCount = 0
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
                }

                val purchase = Purchase(shop.id, randomProducts)
                val producerRecord = ProducerRecord("purchases", purchase.shopId, purchase)
                producer.send(producerRecord) { _, exception ->
                    when (exception) {
                        null -> successCount++
                        else -> {
                            exception.printStackTrace()
                            failCount++
                        }
                    }
                }

                Thread.sleep(2)
            }
            println("Purchase producing stopped!")
        }
    }

}
