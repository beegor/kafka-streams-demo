package digital.porsche.ks.model

import java.math.BigDecimal

data class Product (
    val id: String,
    val name: String,
    val buyingPrice: BigDecimal,
    val sellingPrice: BigDecimal
)