package digital.porsche.ks.model

data class ProductPurchase (
    val productId: String,
    val amount: Int) {

    operator fun plus(v2: ProductPurchase) =
        this.copy(productId = productId, amount = amount + (v2.amount))
}