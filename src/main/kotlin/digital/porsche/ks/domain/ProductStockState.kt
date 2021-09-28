package digital.porsche.ks.domain

import kotlin.math.max

data class ProductStockState(
    val productState:MutableMap<String, Int> = mutableMapOf()
) {

    fun addProducts(productId: String, amount: Int){
        productState[productId] = (productState[productId] ?: 0) + amount
    }

    fun removeProducts(productId: String, amount: Int) {
        val currentState = productState[productId] ?: 0
        val newState = currentState - amount
        productState[productId] = newState
    }
}
