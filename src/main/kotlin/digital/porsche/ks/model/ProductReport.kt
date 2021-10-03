package digital.porsche.ks.model

import java.math.BigDecimal

data class ProductReport (
    val productId: String,
    val productName: String,
    val amountSold: Int,
    val profit: BigDecimal
)