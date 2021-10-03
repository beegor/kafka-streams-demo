package digital.porsche.ks.service

import digital.porsche.ks.model.Constants
import digital.porsche.ks.model.Product
import digital.porsche.ks.model.ProductsOffered
import digital.porsche.ks.serializers.JsonSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class ProductManager {

    @PostConstruct
    fun onStart() {
        val config = Properties()
        config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        val producer = KafkaProducer<String, Product>(config)

        ProductsOffered.get()
            .forEach {
                val record = ProducerRecord(Constants.TOPIC_PRODUCTS, it.id, it)
                producer.send(record) { recordMetadata, exception ->
                    exception?.run { printStackTrace() }
                    recordMetadata?.run { println("Product \"${it.name}\" stored to Kafka successfully") }
                }
            }
    }

}