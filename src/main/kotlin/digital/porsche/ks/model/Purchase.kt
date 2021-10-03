package digital.porsche.ks.model

class Purchase (
    val shopId: String,

    /**
     * Map of purchased products, key representing product id,
     * and value representing amount (number of products)
     */
    val products: Map<String, Int>
)