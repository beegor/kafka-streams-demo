package digital.porsche.ks.serializers

import digital.porsche.ks.model.ProductProfitHistory
import digital.porsche.ks.model.ProductStockState
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

fun <T>createJSONSerde(tClass: Class<T>): Serde<T> {
    return Serdes.serdeFrom(JsonSerializer<T>(), JsonDeserializer(tClass))
}

class StockStateDeserializer() : JsonDeserializer<ProductStockState>(ProductStockState::class.java)
class ProductProfitHistoryDeserializer() : JsonDeserializer<ProductProfitHistory>(ProductProfitHistory::class.java)
