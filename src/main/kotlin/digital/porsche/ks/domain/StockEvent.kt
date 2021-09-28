package digital.porsche.ks.domain

class StockEvent (
    val eventType: StockEventType,
    val productId: String,
    val amount: Int
)