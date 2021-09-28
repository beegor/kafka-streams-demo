package digital.porsche.ks.web

import digital.porsche.ks.domain.ProductsOffered
import digital.porsche.ks.service.StockManager
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*

@Controller
class TransactionsController(
    private val stockManager: StockManager
) {


    @GetMapping("/stock-state/{shopId}")
    fun showStockState( @PathVariable shopId: String, model: Model ): String {
        model.addAttribute("stockLabels" , ProductsOffered.get().map { it.id }.toTypedArray())
        model.addAttribute("shopId", shopId)
        return "tx-count"
    }

    @GetMapping("/stock-state-data/{shopId}")
    @ResponseBody
    fun getStockState(@PathVariable shopId: String): Map<String, Any> {

        val labels = ProductsOffered.get().map { it.id }
        val data = labels.map { stockManager.getStockState(shopId)?.productState?.get(it) ?: 0 }

        labels.forEachIndexed { i, productId ->
            if (data[i] <= 0)
                stockManager.restock(shopId, productId)
        }

        return mapOf<String, Any>(
            "stockLabels" to labels.toTypedArray(),
            "stockStateValues" to data.toTypedArray()
        )
    }






}