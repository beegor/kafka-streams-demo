package digital.porsche.ks.model

class StockEvent (
    val eventType: StockEventType,
    val productId: String,
    val amount: Int
)