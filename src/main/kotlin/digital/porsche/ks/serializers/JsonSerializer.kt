package digital.porsche.ks.serializers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Serializer

open class JsonSerializer<T> : Serializer<T> {

    private val mapper = jacksonObjectMapper()

    override fun serialize(topic: String, data: T): ByteArray {
        return mapper.writeValueAsBytes(data)
    }
}