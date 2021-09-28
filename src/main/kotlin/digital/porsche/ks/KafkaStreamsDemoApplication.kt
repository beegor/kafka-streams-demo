package digital.porsche.ks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaStreamsDemoApplication

fun main(args: Array<String>) {
	runApplication<KafkaStreamsDemoApplication>(*args)
}
