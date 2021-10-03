package digital.porsche.ks.model

import java.math.BigDecimal

object ProductsOffered {

    fun get() = listOf (
        Product("000001", "Milk 1L", BigDecimal(0.7), BigDecimal(1.0)),
        Product("000002", "Bread 500g", BigDecimal(0.5), BigDecimal(0.8)),
        Product("000003", "Bananas 1kg", BigDecimal(1), BigDecimal(1.3)),
        Product("000004", "Apples 1kg", BigDecimal(0.5), BigDecimal(0.9)),
        Product("000005", "Ice cream", BigDecimal(3.2), BigDecimal(3.7)),
        Product("000006", "Beer 0.5L", BigDecimal(0.6), BigDecimal(0.9)),
        Product("000007", "Cigarettes ", BigDecimal(2.0), BigDecimal(2.5)),
        Product("000008", "Orange juice 1L", BigDecimal(1.8), BigDecimal(2.2)),
        Product("000009", "Coffee 2dl", BigDecimal(0.8), BigDecimal(1.2)),
    )
}