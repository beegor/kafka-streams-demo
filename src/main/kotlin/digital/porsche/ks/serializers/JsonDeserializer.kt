package digital.porsche.ks.serializers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer

open class JsonDeserializer<T>(private val tClass: Class<T>) : Deserializer<T> {

    private val mapper = jacksonObjectMapper()

    override fun deserialize(topic: String, data: ByteArray): T {
        return mapper.readValue(data, tClass)
    }
}