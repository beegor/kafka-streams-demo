package digital.porsche.ks.serializers

import digital.porsche.ks.domain.ProductStockState
import digital.porsche.ks.domain.Purchase
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

fun <T>createJSONSerde(tClass: Class<T>): Serde<T> {
    return Serdes.serdeFrom(JsonSerializer<T>(), JsonDeserializer(tClass))
}

class PurchaseSerializer() : JsonSerializer<Purchase>()
class StockStateDeserializer() : JsonDeserializer<ProductStockState>(ProductStockState::class.java)
